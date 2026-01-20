package ca.quines.namingconverter;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Stack;
import java.util.function.UnaryOperator;

import org.eclipse.swt.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NamingConventionConverterGui extends Dialog {

    private Text mainTextArea;
    private Button undoBtn;
    private final Stack<String> undoStack = new Stack<>();

    public NamingConventionConverterGui(Shell parent) {
        super(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
        setText("Naming Convention Converter");
    }

    public void open() {
        Shell parent = getParent();
        Shell shell = new Shell(parent, SWT.SHELL_TRIM);
        shell.setText(getText());
        shell.setLayout(new GridLayout(1, true));

        // Top Section: Text Area
        mainTextArea = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

        // Allow it to expand as the window expands.
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.heightHint = 150; 
        textData.minimumHeight = 100;
        mainTextArea.setLayoutData(textData);

        // Provide our own Undo Logic when Ctrl+Z is typed.
        mainTextArea.addListener(SWT.KeyDown, e -> {
            boolean isUndoKey = (e.stateMask == SWT.MOD1) && (e.keyCode == 'z');
            if (isUndoKey && !undoStack.isEmpty()) {
                e.doit = false;
                mainTextArea.setText(undoStack.pop());
                undoBtn.setEnabled(!undoStack.isEmpty());
            }
        });

        // Middle Section: Undo Button
        undoBtn = new Button(shell, SWT.PUSH);
        undoBtn.setText("Undo");
        undoBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        undoBtn.setEnabled(false);
        setupUndo(undoBtn);

        // Bottom Section: Tab Folder for Categories
        TabFolder folder = new TabFolder(shell, SWT.NONE);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createMiscellaneousTab(folder);
        createCaseTab(folder);
        createSqlTab(folder);
        createRegexTab(shell, folder);

        // 4. Help Button (Bottom)
        Button helpBtn = new Button(shell, SWT.PUSH);
        helpBtn.setText("Help / Doc");
        helpBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        helpBtn.addListener(SWT.Selection, e -> openHelpWindow(shell.getDisplay()));

        shell.pack();
        shell.setMinimumSize(700, 500);
        shell.open();

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

	private void createMiscellaneousTab(TabFolder folder) {
        TabItem miscTab = new TabItem(folder, SWT.NONE);
        miscTab.setText("Miscellaneous");
        Composite miscGroup = createTabComposite(folder, 4);
        miscTab.setControl(miscGroup);

        createButton(miscGroup, "Remove Dashes and Trim", TextTransformers::removeDashesAndTrim);
        createButton(miscGroup, "Replace Dashes with Spaces", TextTransformers::replaceDashesWithSpaces);
        createButton(miscGroup, "Change Underscores to Spaces", TextTransformers::underscoresToSpaces);
        createButton(miscGroup, "Replace Whitespace\nwith One Space and Trim", TextTransformers::collapseWhitespace);
        createButton(miscGroup, "Spaces to camelCase", s -> TextTransformers.toCamelCase(s, false));
        createButton(miscGroup, "Spaces to CamelCase", s -> TextTransformers.toCamelCase(s, true));
        createButton(miscGroup, "CamelCase to Spaces", TextTransformers::camelCaseToSpaces);
        createPlaceholder(miscGroup);
        createButton(miscGroup, "Spaces to UPPER_SNAKE_CASE", TextTransformers::spacesToUpperSnake);
        createButton(miscGroup, "Camel Case to UPPER_SNAKE_CASE", TextTransformers::camelCaseToUpperSnake);
        createButton(miscGroup, "UPPER_SNAKE_CASE to camelCase", s -> TextTransformers.snakeToCamel(s, false));
        createButton(miscGroup, "UPPER_SNAKE_CASE\nto CamelCase", s -> TextTransformers.snakeToCamel(s, true));
        createButton(miscGroup, "Decode URL", s -> URLDecoder.decode(s, StandardCharsets.UTF_8));
        createButton(miscGroup, "Encode URL", s -> URLEncoder.encode(s, StandardCharsets.UTF_8));
	}

	private void createCaseTab(TabFolder folder) {
        TabItem caseTab = new TabItem(folder, SWT.NONE);
        caseTab.setText("Case");
        Composite caseGroup = createTabComposite(folder, 3);
        caseTab.setControl(caseGroup);

        createButton(caseGroup, "UPPERCASE", TextTransformers::toUpperCase);
        createButton(caseGroup, "lowercase", TextTransformers::toLowerCase);
        createButton(caseGroup, "All Initial Capitals", TextTransformers::allInitialCaps);
        createButton(caseGroup, "Convert to Title Case", TextTransformers::toTitleCase);
        createButton(caseGroup, "lowercase First Character", TextTransformers::lowercaseFirst);
        createButton(caseGroup, "Uppercase First Character", TextTransformers::uppercaseFirst);
	}

	private void createSqlTab(TabFolder folder) {
        TabItem sqlTab = new TabItem(folder, SWT.NONE);
        sqlTab.setText("SQL");
        Composite sqlGroup = createTabComposite(folder, 4);
        sqlTab.setControl(sqlGroup);

        createButton(sqlGroup, "Newline separated to Comma Delimited", TextTransformers::newlineToComma);
        createButton(sqlGroup, "Newline separated to Quoted Comma Delimited", TextTransformers::newlineToQuotedComma);
        createButton(sqlGroup, "In Clause for Integers", TextTransformers::toInClauseInt);
        createButton(sqlGroup, "In Clause for Strings", TextTransformers::toInClauseString);
        createButton(sqlGroup, "Clean Up Autogenerated SQL", s -> s);
        createButton(sqlGroup, "Convert from Java to SQL", s -> s);
        createButton(sqlGroup, "Convert from SQL to Java String Concatenation", s -> s);
        createButton(sqlGroup, "Convert from SQL\nto String Buffer Appends", s -> s);
	}

	private void createRegexTab(Shell shell, TabFolder folder) {
        TabItem regexTab = new TabItem(folder, SWT.NONE);
        regexTab.setText("Regex");
        Composite regexGroup = createTabComposite(folder, 1);
        regexTab.setControl(regexGroup);

        Composite inputRow = new Composite(regexGroup, SWT.NONE);
		inputRow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		inputRow.setLayout(new GridLayout(5, false));
		
		new Label(inputRow, SWT.NONE).setText("Find:");
		Text findText = new Text(inputRow, SWT.BORDER);
		findText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		new Label(inputRow, SWT.NONE).setText("Replace:");
		Text replaceText = new Text(inputRow, SWT.BORDER);
		replaceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Button regexGoBtn = new Button(inputRow, SWT.PUSH);
		regexGoBtn.setText("  Go  ");
		regexGoBtn.addListener(SWT.Selection, e -> {
		    String pattern = findText.getText();
		    String replacement = replaceText.getText();
		    String current = mainTextArea.getText();
		    if (pattern.isEmpty()) return;
		    try {
		        undoStack.push(current);
		        undoBtn.setEnabled(true);
		        mainTextArea.setText(current.replaceAll(pattern, replacement));
		    } catch (Exception ex) {
		        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		        mb.setText("Regex Error");
		        mb.setMessage("Invalid Regex: " + ex.getMessage());
		        mb.open();
		    }
		});
	}

    private Composite createTabComposite(TabFolder folder, int columns) {
        Composite composite = new Composite(folder, SWT.NONE);
        GridLayout layout = new GridLayout(columns, true);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        composite.setLayout(layout);
        return composite;
    }

    private void createButton(Composite parent, String text, UnaryOperator<String> transformer) {
        Button b = new Button(parent, SWT.PUSH | SWT.WRAP);
        b.setText(text);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 45;
        b.setLayoutData(gd);

        b.addListener(SWT.Selection, e -> {
            String currentText = mainTextArea.getText();
            if (currentText == null || currentText.isEmpty()) return;
            undoStack.push(currentText);
            if (undoBtn != null) undoBtn.setEnabled(true);
            mainTextArea.setText(transformer.apply(currentText));
        });
    }

    private void setupUndo(Button undoBtn) {
        undoBtn.addListener(SWT.Selection, e -> {
            if (!undoStack.isEmpty()) {
                mainTextArea.setText(undoStack.pop());
                undoBtn.setEnabled(!undoStack.isEmpty());
            }
        });
    }

    private void createPlaceholder(Composite parent) {
        new Label(parent, SWT.NONE);
    }

    private void openHelpWindow(Display display) {
        Shell helpShell = new Shell(display, SWT.SHELL_TRIM);
        helpShell.setText("Documentation");
        helpShell.setLayout(new FillLayout());
        helpShell.setSize(800, 400);

        try {
            Browser browser = new Browser(helpShell, SWT.NONE);

            String html = "<html><body style='font-family: sans-serif; padding: 20px; line-height: 1.6;'>"
                    + "<h2>Naming Convention Converter Help</h2>"
                    + "<h3>Shortcut Keys</h3>"
                    + "<p>Standard Ctrl+C (Copy), Ctrl+V (Paste), and Ctrl+A (Select All) work within the text area.</p>"
                    + "<h3>SQL Conversion Tips</h3>"
                    + "<p>Use <b>'In Clause for Strings'</b> when you have a list of IDs and need to paste them into a <code>WHERE id IN (...)</code> query.</p>"
                    + "<h3>Regex Flags</h3>"
                    + "<p>You can add the following at the very beginning of your 'Find:' string.</p>"
                    + "<ul>"
                    + "  <li><b>(?i) Ignore Case:</b> Matches 'ABC' and 'abc' identically.</li>"
                    + "  <li><b>(?m) Multiline:</b> ^ and $ match the start/end of <i>each line</i> instead of the whole text.</li>"
                    + "  <li><b>(?s) Dotall:</b> The '.' character will match newline characters.</li>"
                    + "</ul>"
                    + "</body></html>";

            browser.setText(html);
        } catch (SWTError e) {
            Text fallback = new Text(helpShell, SWT.MULTI | SWT.WRAP);
            fallback.setText("Regex Help\nAdd the following to the beginning of your Find text:  (?i) = Ignore Case\n(?m) = Multiline");
            fallback.setEditable(false);
        }
        helpShell.open();
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display); 
        NamingConventionConverterGui gui = new NamingConventionConverterGui(shell);
        gui.open();
        display.dispose();
    }

}

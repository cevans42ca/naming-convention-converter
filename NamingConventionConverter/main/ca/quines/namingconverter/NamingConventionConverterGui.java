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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;

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
        // Create a new shell for the dialog
        Shell shell = new Shell(parent, SWT.SHELL_TRIM);
        shell.setText(getText());

        // 1. Set the main layout
        shell.setLayout(new GridLayout(1, true));

        // 2. The Text Area
        mainTextArea = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

        GC gc = new GC(mainTextArea);
        FontMetrics fm = gc.getFontMetrics();
        int heightHint = fm.getHeight() * 3;
        gc.dispose();

        // IMPORTANT: We give it a heightHint so it doesn't take over the whole screen
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.heightHint = heightHint;
        textData.minimumHeight = 100;
        mainTextArea.setLayoutData(textData);

        // Grab the user's use of Ctrl+Z and hook it into our undo solution to keep things consistent.
        mainTextArea.addListener(SWT.KeyDown, e -> {
            // Check for Ctrl+Z (or Cmd+Z on Mac)
            boolean isUndoKey = (e.stateMask == SWT.MOD1) && (e.keyCode == 'z');
            
            if (isUndoKey) {
                // Stop the native widget from doing its own undo
                e.doit = false;
                
                // Trigger your custom undo logic
                if (!undoStack.isEmpty()) {
                    mainTextArea.setText(undoStack.pop());
                    undoBtn.setEnabled(!undoStack.isEmpty());
                }
            }
        });
        
        // 3. The Undo Button
        undoBtn = new Button(shell, SWT.PUSH);
        undoBtn.setText("Undo");
        undoBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        setupUndo(undoBtn);

        // --- SECTION: Miscellaneous (4 Columns) ---
        Composite miscGroup = createSectionGroup(shell, "Miscellaneous", 4);
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
       	createButton(miscGroup, "UPPER_SNAKE_CASE to camelCase", TextTransformers::snakeToCamelCase);
       	createButton(miscGroup, "UPPER_SNAKE_CASE\nto CamelCase", TextTransformers::snakeToPascalCase);
    	createButton(miscGroup, "Decode URL", s -> URLDecoder.decode(s, StandardCharsets.UTF_8));
        createButton(miscGroup, "Encode URL", s -> URLEncoder.encode(s, StandardCharsets.UTF_8));
        createPlaceholder(miscGroup);
        createPlaceholder(miscGroup);

        // --- SECTION: Case (3 Columns) ---
        Composite caseGroup = createSectionGroup(shell, "Case", 3);
        createButton(caseGroup, "UPPERCASE", TextTransformers::toUpperCase);
        createButton(caseGroup, "lowercase", TextTransformers::toLowerCase);
        createButton(caseGroup, "All Initial Capitals", TextTransformers::allInitialCaps);
        createButton(caseGroup, "Convert to Title Case", TextTransformers::toTitleCase);
        createButton(caseGroup, "lowercase First Character", TextTransformers::lowercaseFirst);
        createButton(caseGroup, "Uppercase First Character", TextTransformers::uppercaseFirst);

        // --- SECTION: SQL (4 Columns) ---
        Composite sqlGroup = createSectionGroup(shell, "SQL", 4);
        createButton(sqlGroup, "Newline separated to Comma Delimited", TextTransformers::newlineToComma);
        createButton(sqlGroup, "Newline separated to Quoted Comma Delimited", TextTransformers::newlineToQuotedComma);
        createButton(sqlGroup, "In Clause for Integers", TextTransformers::toInClauseInt);
        createButton(sqlGroup, "In Clause for Strings", TextTransformers::toInClauseString);
        createButton(sqlGroup, "Clean Up Autogenerated SQL", s -> s);
        createButton(sqlGroup, "Convert from Java to SQL", s -> s);
        createButton(sqlGroup, "Convert from SQL to Java String Concatenation", s -> s);
        createButton(sqlGroup, "Convert from SQL\nto String Buffer Appends", s -> s);

        // --- SECTION: Regex ---
        createRegexSection(shell);

        // --- HELP BUTTON (Bottom Centered) ---
        Button helpBtn = new Button(shell, SWT.PUSH);
        helpBtn.setText("Help / Doc");
        GridData helpData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        helpData.verticalIndent = 10;
        helpBtn.setLayoutData(helpData);

        helpBtn.addListener(SWT.Selection, e -> openHelpWindow(shell.getDisplay()));

        // 4. THE CRITICAL REFRESH SEQUENCE
        shell.pack();         // Sizes the window to fit the preferred size of all buttons
        shell.layout(true);   // Forces the layout engine to position every widget
        shell.open();         // Actually shows the window

        // Standard SWT event loop
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

	private void createRegexSection(Shell shell) {
		// --- SECTION: Regex (1 Column for the header, then a sub-grid) ---
        Composite regexGroup = createSectionGroup(shell, "Regex", 1);

        Composite inputRow = new Composite(regexGroup, SWT.NONE);
        inputRow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout rowLayout = new GridLayout(5, false); // 5 elements on one line
        rowLayout.marginWidth = 0;
        inputRow.setLayout(rowLayout);

        // 1. Label
        new Label(inputRow, SWT.NONE).setText("Find:");

        // 2. Regex Input
        Text findText = new Text(inputRow, SWT.BORDER);
        findText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        findText.setMessage("Regex pattern...");

        // 3. Label
        new Label(inputRow, SWT.NONE).setText("Replace:");

        // 4. Replacement Input
        Text replaceText = new Text(inputRow, SWT.BORDER);
        replaceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        replaceText.setMessage("Replacement...");

        // 5. Go Button
        Button goBtn = new Button(inputRow, SWT.PUSH);
        goBtn.setText("  Go  ");
        goBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        // --- The Logic ---
        goBtn.addListener(SWT.Selection, e -> {
            String pattern = findText.getText();
            String replacement = replaceText.getText();
            String current = mainTextArea.getText();
            
            if (pattern.isEmpty()) return;

            try {
                undoStack.push(current);
                undoBtn.setEnabled(true);
                String result = current.replaceAll(pattern, replacement);
                mainTextArea.setText(result);
            }
            catch (Exception ex) {
                // Handle invalid regex patterns gracefully
                MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                mb.setText("Regex Error");
                mb.setMessage("Invalid Regex: " + ex.getMessage());
                mb.open();
            }
        });
	}

    /**
     * Creates a container for a section with a specific number of columns.
     */
    private Composite createSectionGroup(Composite parent, String title, int columns) {
        // 1. The Main Container for the section
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        // 2. Cross-platform Bold Header
        Label label = new Label(container, SWT.NONE);
        label.setText(title);
        
        // Get the actual system font (Segoe UI on Win, San Francisco on Mac)
        FontData[] fontData = parent.getDisplay().getSystemFont().getFontData();
        for (FontData fd : fontData) {
            fd.setStyle(SWT.BOLD);
            fd.setHeight(10);
        }
        Font headerFont = new Font(parent.getDisplay(), fontData);
        label.setFont(headerFont);
        
        // Cleanup: Dispose of the font when the label is destroyed
        label.addDisposeListener(e -> headerFont.dispose());

        // 3. The Button Grid
        Composite buttonGrid = new Composite(container, SWT.NONE);
        // 'true' forces columns to be identical in width
        GridLayout layout = new GridLayout(columns, true); 
        layout.marginWidth = 0; 
        layout.marginHeight = 5;
        buttonGrid.setLayout(layout);
        buttonGrid.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        return buttonGrid;
    }

    private void createButton(Composite parent, String text, UnaryOperator<String> transformer) {
        // SWT.WRAP is important for long button text
        Button b = new Button(parent, SWT.PUSH | SWT.WRAP);
        b.setText(text);
        
        // This restores the "full width" behavior
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 40; // Keeps buttons uniform in height
        b.setLayoutData(gd);

        b.addListener(SWT.Selection, e -> {
            String currentText = mainTextArea.getText();
            if (currentText == null || currentText.isEmpty()) return;

            // Save current state to stack before transforming
            undoStack.push(currentText);
            
            // Enable the undo button if you've made it a class field
            if (undoBtn != null) {
                undoBtn.setEnabled(true);
            }

            // Apply logic and update UI
            String transformed = transformer.apply(currentText);
            mainTextArea.setText(transformed);
        });
    }

    private void setupUndo(Button undoBtn) {
        undoBtn.addListener(SWT.Selection, e -> {
            if (!undoStack.isEmpty()) {
                mainTextArea.setText(undoStack.pop());
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
        helpShell.setSize(800, 600);

        try {
            Browser browser = new Browser(helpShell, SWT.NONE);

            // Define your documentation as HTML
            String html = "<html><body style='font-family: sans-serif; padding: 20px; line-height: 1.6;'>"
                    + "<h2>Naming Convention Converter Help</h2>"
                    + "<h3>Regex Flags</h3>"
                    + "<p>You can add the following at the very beginning of your 'Find:' string.</p>"
                    + "<ul>"
                    + "  <li><b>(?i) Ignore Case:</b> Matches 'ABC' and 'abc' identically.</li>"
                    + "  <li><b>(?m) Multiline:</b> ^ and $ match the start/end of <i>each line</i> instead of the whole text.</li>"
                    + "  <li><b>(?s) Dotall:</b> The '.' character will match newline characters.</li>"
                    + "</ul>"
                    + "<h3>SQL Conversion Tips</h3>"
                    + "<p>Use <b>'In Clause for Strings'</b> when you have a list of IDs and need to paste them into a <code>WHERE id IN (...)</code> query.</p>"
                    + "<h3>Shortcut Keys</h3>"
                    + "<p>Standard Ctrl+V (Paste) and Ctrl+A (Select All) work within the text area.</p>"
                    + "</body></html>";

            browser.setText(html);
        }
        catch (SWTError e) {
            // Fallback if no browser component is installed (rare on modern OSs)
            Text fallback = new Text(helpShell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
            fallback.setText("Regex Help\nAdd the following to the beginning of your Find text:  (?i) = Ignore Case\n(?m) = Multiline");
            fallback.setEditable(false);
        }

        helpShell.open();
    }

    public static void main(String[] args) {
        Display display = new Display();
        // This is the "parent" shell
        Shell shell = new Shell(display); 
        
        // Create and open your GUI
        NamingConventionConverterGui gui = new NamingConventionConverterGui(shell);
        gui.open();
        
        display.dispose();
    }

}

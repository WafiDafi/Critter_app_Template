
package critterapp2;


import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CritterApp2{
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new CritterGUIWithSettings());
    }
}
/**
 * Core business logic and state tracking for the virtual pet.
 */
class Critter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int full = 10;
    private int happy = 10;
    private int age = 0;
    private boolean asleep = false;
    private int treats = 3;
    private int cleanliness = 10;

    public Critter(String name, int startAge) {
        this.name = (name == null || name.isBlank()) ? "Critter" : name;
        setAge(startAge);
    }
    
    //Getters
    public String getName() { return name; }
    public int getFull() { return full; }
    public int getHappy() { return happy; }
    public int getAge() { return age; }
    public boolean isAsleep() { return asleep; }
    public int getTreats() { return treats; }
    public int getCleanliness() { return cleanliness; }
    //Setter and Mutators
    public void setName(String newName) { if (newName == null) return; if (newName.length() > 20) newName = newName.substring(0,20); this.name = newName; }
    public void setAge(int startAge) { this.age = Math.max(0, startAge); }
     /**
     * Determines text dialogue output depending on the pet's current state.
     */
    public String talk() {
        if (!isAlive()) return name + " isn't responding... 💀";
        if (asleep) return name + " is sleeping... 😴";
        if (happy > 6) return "Hi! My name is " + name + " — I'm happy! 😊";
        if (happy > 3) return name + " is OK right now. 🙂";
        return name + " feels sad... 😕";
    }
    // Core interaction loops, edit as you want
    public void eat() { if (!asleep) { full += 4; happy += 2; capStats(); } }
    public void play() { if (!asleep) { happy += 3; full -= 2; cleanliness -= 1; capStats(); } }
    public void walk() { if (!asleep) { happy += 3; full -= 2; cleanliness -= 2; capStats(); } }
    public void sleep() { asleep = true; }
    public void wakeUp() { asleep = false; }
    public void useTreat() { if (!asleep && treats > 0) { treats--; full += 3; happy += 4; capStats(); } }
    public void clean() { if (!asleep) { cleanliness = 10; happy += 2; capStats(); } }
    public void rename(String newName) { setName(newName); }
    public void age() { age++; full--; happy--; cleanliness--; if (full < 3) happy--; capStats(); }
    public boolean isAlive() { return full > 0 && happy > 0; }
    private void capStats() { if (full > 20) full = 20; if (happy > 20) happy = 20; if (cleanliness > 20) cleanliness = 20; if (full < -5) full = -5; if (happy < -5) happy = -5; if (cleanliness < -5) cleanliness = -5; }
}
  /**
 * Handles application presentation, configuration panels, asset rendering, and sound events.
 */
class CritterGUIWithSettings {
    private JFrame frame;
    private JPanel mainPanel; // contains game UI
    private JPanel settingsPanel; // full-screen settings UI

    // Game components
    private JLabel imgLabel;
    private JLabel statusLabel;
    private JLabel statsLabel;
    private Critter critter;

    // maps
    private Map<String, ImageIcon> moodImages = new HashMap<>();
    private Map<String, File> soundFiles = new HashMap<>();
    private Map<String, ImageIcon> buttonIcons = new HashMap<>(); // key: button name

    // Buttons (kept in map so settings can change icons)
    private Map<String, JButton> gameButtons = new HashMap<>();

    // Settings state
    private File backgroundImageFile = null;
    private boolean soundEnabled = true;
    private boolean darkTheme = false;
    private boolean autoSave = false;
    private int fontSize = 14;
    private Color nameColor = Color.BLACK;
    private double agingFactor = 1.0; // 1.0 = normal

    private final int IMG_W = 360;
    private final int IMG_H = 300;

    // FIX: Changed default button size to a larger square for better image fit
    private final Dimension BUTTON_SIZE = new Dimension(90, 90);

    // Specific UI components we need to manually manage color
    private JCheckBox autoSaveCheckBox;
    private JCheckBox soundEnabledCheckBox;
    private JCheckBox themeCheckBox;

    /**
     * Custom container optimized for painting transparent scaled background textures.
     */
    
    private class BackgroundPanel extends JPanel {
        private Image img;

        public BackgroundPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false); // Make sure the panel itself isn't painting its background
        }

        public void setBackgroundImage(File imageFile, int frameWidth, int frameHeight) {
            this.img = null;
            if (imageFile != null && imageFile.exists()) {
                try {
                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                    // Scale image to frame size
                    this.img = icon.getImage().getScaledInstance(frameWidth, frameHeight, Image.SCALE_SMOOTH);
                } catch (Exception e) {
                    // Ignore
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Set 50% transparency (0.5f)
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                // Draw the image filling the component
                g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                g2d.dispose();
            }
        }
    }


    public CritterGUIWithSettings() {
        String name = JOptionPane.showInputDialog(null, "Enter name for your critter:", "Critter Name", JOptionPane.QUESTION_MESSAGE);
        if (name == null) name = "Critter";
        int startAge = 0;
        String ageStr = JOptionPane.showInputDialog(null, "Enter starting age (number):", "0");
        try { startAge = Integer.parseInt(ageStr); } catch (Exception ignored) {}
        critter = new Critter(name, startAge);

        loadDefaultImages();
        loadDefaultSounds();

        SwingUtilities.invokeLater(this::createAndShowGUI);
    }
    // Asset Loading Infrastructure
    private void loadDefaultImages() {
        moodImages.put("happy", loadScaledIconFromPathIfExists("happy.png", IMG_W, IMG_H));
        moodImages.put("sad", loadScaledIconFromPathIfExists("sad.png", IMG_W, IMG_H));
        moodImages.put("asleep", loadScaledIconFromPathIfExists("asleep.png", IMG_W, IMG_H));
        moodImages.put("dead", loadScaledIconFromPathIfExists("dead.png", IMG_W, IMG_H));
        moodImages.put("dirty", loadScaledIconFromPathIfExists("dirty.png", IMG_W, IMG_H));
        moodImages.put("neutral", loadScaledIconFromPathIfExists("neutral.png", IMG_W, IMG_H));
    }

    private void loadDefaultSounds() {
        putSoundIfExists("feed", "feed.wav");
        putSoundIfExists("play", "play.wav");
        putSoundIfExists("walk", "walk.wav");
        putSoundIfExists("sleep", "sleep.wav");
        putSoundIfExists("wake", "wake.wav");
        putSoundIfExists("treat", "treat.wav");
        putSoundIfExists("clean", "clean.wav");
        putSoundIfExists("rename", "rename.wav");
        putSoundIfExists("click", "click.wav");
    }

    private void putSoundIfExists(String key, String filename) {
        File f = new File(filename);
        if (f.exists() && f.isFile()) soundFiles.put(key, f);
    }

    private ImageIcon loadScaledIconFromPathIfExists(String path, int w, int h) {
        File f = new File(path);
        if (!f.exists()) return null; 
        try { 
            ImageIcon icon = new ImageIcon(path); 
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH); 
            return new ImageIcon(img); 
        } catch (Exception e) { 
            System.err.println("Error loading image from path: " + path + " - " + e.getMessage());
            return null; // Return null on loading error
        }
    }

    private void createAndShowGUI() {
        frame = new JFrame("🐾 Critter Pet Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 680);
        frame.setLocationRelativeTo(null);

        // create main and settings panels
        mainPanel = createMainPanel();
        settingsPanel = createSettingsPanel();

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
    /**
     * Builds the primary dashboard window tracking critter rendering, metrics, and interaction hooks.
     */
    private JPanel createMainPanel() {
        // Use the custom panel for background image support
        BackgroundPanel root = new BackgroundPanel(new BorderLayout(10,10));
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false); // Make center panel transparent so background image shows

        imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(IMG_W, IMG_H));
        updateImage();

        statusLabel = new JLabel("<html><center>" + critter.talk() + "</center></html>", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

        center.add(imgLabel, BorderLayout.CENTER);
        center.add(statusLabel, BorderLayout.SOUTH);

        statsLabel = new JLabel(getStatsHtml(), SwingConstants.CENTER);
        statsLabel.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        center.add(statsLabel, BorderLayout.NORTH);

        root.add(center, BorderLayout.CENTER);

        // Buttons panel
        // Use a grid with 2 rows and 5 columns to fit 10 buttons
        JPanel buttons = new JPanel(new GridLayout(2, 5, 8, 8));
        buttons.setOpaque(false); // Make buttons panel transparent
        String[] names = {"Check","Feed","Play","Walk","Rename","Sleep","Wake","Treat","Clean","Settings"};
        for (String n: names) {
            JButton b = new JButton(n);
            b.setPreferredSize(BUTTON_SIZE);
            b.setFocusPainted(false);
            b.setMargin(new Insets(4,6,4,6));
            
            // Set vertical text position to bottom and horizontal text position to center
            b.setVerticalTextPosition(SwingConstants.BOTTOM);
            b.setHorizontalTextPosition(SwingConstants.CENTER);
            
            gameButtons.put(n.toLowerCase(), b);
            buttons.add(wrap(b));
        }

        // Attach actions
        gameButtons.get("check").addActionListener(e -> { playSound("click"); doCheck(); });
        gameButtons.get("feed").addActionListener(e -> { critter.eat(); playSound("feed"); postActionUpdate("You fed " + critter.getName() + "!"); });
        gameButtons.get("play").addActionListener(e -> { critter.play(); playSound("play"); postActionUpdate("You played with " + critter.getName() + "!"); });
        gameButtons.get("walk").addActionListener(e -> { critter.walk(); playSound("walk"); postActionUpdate(critter.getName() + " enjoyed the walk!"); });
        gameButtons.get("rename").addActionListener(e -> { String newName = JOptionPane.showInputDialog(frame, "Enter new name:", critter.getName()); if (newName != null && !newName.isBlank()) { critter.rename(newName); playSound("rename"); } postActionUpdate("Renamed to " + critter.getName()); });
        gameButtons.get("sleep").addActionListener(e -> { critter.sleep(); playSound("sleep"); postActionUpdate(critter.getName() + " is now sleeping."); });
        gameButtons.get("wake").addActionListener(e -> { critter.wakeUp(); playSound("wake"); postActionUpdate(critter.getName() + " woke up!"); });
        gameButtons.get("treat").addActionListener(e -> { critter.useTreat(); playSound("treat"); postActionUpdate("Gave a treat to " + critter.getName()); });
        gameButtons.get("clean").addActionListener(e -> { critter.clean(); playSound("clean"); postActionUpdate("You cleaned " + critter.getName()); });
        gameButtons.get("settings").addActionListener(e -> switchToSettings());

        root.add(buttons, BorderLayout.SOUTH);
        applyBackground(root);
        applyTheme(root);
        updateAllButtonIcons();
        return root;
    }

    private JPanel wrap(JButton btn) {
        JPanel p = new JPanel();
        p.add(btn);
        p.setOpaque(false); // Make wrapper panel transparent
        return p;
    }

    private JPanel createSettingsPanel() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(4,2,8,8));
        JButton bImages = new JButton("Change Images");
        JButton bSounds = new JButton("Change Sounds");
        JButton bBackground = new JButton("Change Background");
        JButton bAppearance = new JButton("Appearance Settings");
        JButton bGame = new JButton("Game Settings");
        JButton bBtnIcons = new JButton("Change Button Icons");
        JButton bReset = new JButton("Reset to Defaults");
        JButton bBack = new JButton("Save & Back");

        center.add(bImages);
        center.add(bSounds);
        center.add(bBackground);
        center.add(bAppearance);
        center.add(bGame);
        center.add(bBtnIcons);
        center.add(bReset);
        center.add(bBack);

        // actions
        bImages.addActionListener(e -> openChangeMoodImagesDialog());
        bSounds.addActionListener(e -> openChangeSoundsDialog());
        bBackground.addActionListener(e -> chooseBackgroundImage());
        bAppearance.addActionListener(e -> openAppearanceDialog());
        bGame.addActionListener(e -> openGameSettingsDialog());
        bBtnIcons.addActionListener(e -> openButtonIconsDialog());
        bReset.addActionListener(e -> { resetDefaults(); JOptionPane.showMessageDialog(frame, "Defaults restored."); });
        bBack.addActionListener(e -> { saveSettings(); switchToMain(); });

        root.add(center, BorderLayout.CENTER);
        
        // --- START OF SOUTH PANEL CREATION AND CHECKBOX HANDLING ---
        JPanel south = new JPanel();
        
        // Assign CheckBoxes to member variables
        autoSaveCheckBox = new JCheckBox("Auto-save", autoSave);
        soundEnabledCheckBox = new JCheckBox("Sound Enabled", soundEnabled);
        themeCheckBox = new JCheckBox("Dark Theme", darkTheme);
        
        // Set their foreground to BLACK permanently (as requested)
        autoSaveCheckBox.setForeground(Color.BLACK);
        soundEnabledCheckBox.setForeground(Color.BLACK);
        themeCheckBox.setForeground(Color.BLACK);
        
        // Attach Listeners
        autoSaveCheckBox.addActionListener(e -> autoSave = autoSaveCheckBox.isSelected());
        
        themeCheckBox.addActionListener(e -> { 
            darkTheme = themeCheckBox.isSelected(); 
            // Apply theme to both panels when toggle changes (will skip these specific boxes)
            applyTheme(settingsPanel); 
            applyTheme(mainPanel); 
            frame.revalidate(); 
            frame.repaint(); 
        });
        
        soundEnabledCheckBox.addActionListener(e -> soundEnabled = soundEnabledCheckBox.isSelected());
        
        south.add(autoSaveCheckBox); 
        south.add(soundEnabledCheckBox); 
        south.add(themeCheckBox);
        root.add(south, BorderLayout.SOUTH);
        // --- END OF SOUTH PANEL CREATION AND CHECKBOX HANDLING ---

        applyTheme(root); // Apply theme to set initial color of other components
        return root;
    }

    private void openChangeMoodImagesDialog() {
        String[] moods = {"happy","sad","asleep","dead","dirty","neutral"};
        JPanel panel = new JPanel(new GridLayout(moods.length, 3, 6,6));
        JFileChooser chooser = new JFileChooser();
        Map<String, JTextField> fields = new HashMap<>();
        for (String m: moods) {
            panel.add(new JLabel(m.substring(0,1).toUpperCase()+m.substring(1)+": "));
            JTextField f = new JTextField(); fields.put(m,f); panel.add(f);
            JButton browse = new JButton("Browse");
            browse.addActionListener(e -> { if (chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) { File sel = chooser.getSelectedFile(); f.setText(sel.getAbsolutePath()); } });
            panel.add(browse);
        }
        int res = JOptionPane.showConfirmDialog(frame, panel, "Set mood images", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res==JOptionPane.OK_OPTION) {
            for (String m: moods) {
                String path = fields.get(m).getText();
                if (path!=null && !path.isBlank()) {
                    ImageIcon ic = loadScaledIconFromPath(path, IMG_W, IMG_H);
                    if (ic!=null) moodImages.put(m, ic);
                }
            }
            updateImage();
        }
    }

    private ImageIcon loadScaledIconFromPath(String path, int w, int h) {
        try { ImageIcon icon = new ImageIcon(path); Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH); return new ImageIcon(img); } catch (Exception e) { return null; }
    }

    private void openChangeSoundsDialog() {
        String[] keys = {"feed","play","walk","sleep","wake","treat","clean","rename","click"};
        JPanel panel = new JPanel(new GridLayout(keys.length,3,6,6));
        JFileChooser chooser = new JFileChooser();
        Map<String, JTextField> fields = new HashMap<>();
        for (String k: keys) {
            panel.add(new JLabel(k+": "));
            JTextField f = new JTextField(); fields.put(k,f); panel.add(f);
            JButton browse = new JButton("Browse");
            browse.addActionListener(e -> { if (chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) { File sel = chooser.getSelectedFile(); f.setText(sel.getAbsolutePath()); } });
            panel.add(browse);
        }
        int res = JOptionPane.showConfirmDialog(frame, panel, "Set sound files (wav)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res==JOptionPane.OK_OPTION) {
            soundFiles.clear();
            for (String k: keys) {
                String path = fields.get(k).getText();
                if (path!=null && !path.isBlank()) { File f = new File(path); if (f.exists()) soundFiles.put(k, f); }
            }
        }
    }

    private void chooseBackgroundImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) {
            File sel = chooser.getSelectedFile(); 
            backgroundImageFile = sel; 
            applyBackground(mainPanel); 
            frame.revalidate(); 
            frame.repaint();
        }
    }

    private void openAppearanceDialog() {
        JPanel panel = new JPanel(new GridLayout(3,2,6,6));
        panel.add(new JLabel("Font size:"));
        JSpinner sp = new JSpinner(new SpinnerNumberModel(fontSize, 10, 24, 1)); panel.add(sp);
        panel.add(new JLabel("Name color:"));
        JButton colorBtn = new JButton("Choose color"); panel.add(colorBtn);
        JLabel preview = new JLabel("Preview: " + critter.getName()); panel.add(preview);
        colorBtn.addActionListener(e -> { Color c = JColorChooser.showDialog(frame, "Choose name color", nameColor); if (c!=null) { nameColor = c; preview.setForeground(c); } });
        int res = JOptionPane.showConfirmDialog(frame, panel, "Appearance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res==JOptionPane.OK_OPTION) { fontSize = (int) sp.getValue(); statusLabel.setFont(new Font("SansSerif", Font.PLAIN, fontSize)); statsLabel.setFont(new Font("Monospaced", Font.PLAIN, fontSize)); }
    }

    private void openGameSettingsDialog() {
        JPanel panel = new JPanel(new GridLayout(3,2,6,6));
        panel.add(new JLabel("Aging speed (1.0 normal):"));
        JSpinner sp = new JSpinner(new SpinnerNumberModel(agingFactor, 0.2, 5.0, 0.1)); panel.add(sp);
        JCheckBox autoSaveBox = new JCheckBox("Auto-save", autoSave); panel.add(autoSaveBox);
        JCheckBox soundBox = new JCheckBox("Sound enabled", soundEnabled); panel.add(soundBox);
        int res = JOptionPane.showConfirmDialog(frame, panel, "Game Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res==JOptionPane.OK_OPTION) { agingFactor = ((Number)sp.getValue()).doubleValue(); autoSave = autoSaveBox.isSelected(); soundEnabled = soundBox.isSelected(); }
    }

    private void openButtonIconsDialog() {
        String[] names = {"Check","Feed","Play","Walk","Rename","Sleep","Wake","Treat","Clean","Settings"};
        JPanel panel = new JPanel(new GridLayout(names.length,4,6,6));
        JFileChooser chooser = new JFileChooser();
        Map<String, JTextField> fields = new HashMap<>();
        for (String n: names) {
            panel.add(new JLabel(n+": "));
            JTextField f = new JTextField(); fields.put(n,f); panel.add(f);
            JButton browse = new JButton("Browse");
            browse.addActionListener(e -> { if (chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) { File sel = chooser.getSelectedFile(); f.setText(sel.getAbsolutePath()); } });
            panel.add(browse);
            JButton remove = new JButton("Remove Icon");
            remove.addActionListener(e -> { buttonIcons.remove(n.toLowerCase()); updateAllButtonIcons(); });
            panel.add(remove);
        }
        int res = JOptionPane.showConfirmDialog(frame, panel, "Change Button Icons (icons will appear centered, text will be below)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res==JOptionPane.OK_OPTION) {
            for (String n: names) {
                String path = fields.get(n).getText();
                if (path!=null && !path.isBlank()) {
                    // Use a slightly smaller dimension for the icon inside the 90x90 button
                    ImageIcon ic = loadAndScaleIconForButton(path, BUTTON_SIZE.width, BUTTON_SIZE.height); 
                    if (ic!=null) {
                        // store a scaled icon and set its description to path so it can be saved later if needed
                        ic.setDescription(path);
                        buttonIcons.put(n.toLowerCase(), ic);
                    }
                }
            }
            updateAllButtonIcons();
        }
    }

    private ImageIcon loadAndScaleIconForButton(String path, int w, int h) {
        // Reduced scaling size (e.g., w-8, h-8) to w-20, h-20 to allow space for the text label below the icon.
        try { ImageIcon icon = new ImageIcon(path); Image img = icon.getImage().getScaledInstance(w-20, h-20, Image.SCALE_SMOOTH); return new ImageIcon(img); } catch (Exception e) { return null; }
    }

    private void updateAllButtonIcons() {
        for (Map.Entry<String, JButton> e: gameButtons.entrySet()) {
            String key = e.getKey(); JButton btn = e.getValue(); ImageIcon ic = buttonIcons.get(key);
            if (ic!=null) {
                btn.setIcon(ic);
                // Position icon above text
                btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);
                btn.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                btn.setIcon(null);
                btn.setVerticalTextPosition(SwingConstants.CENTER);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);
            }
        }
    }

    private void resetDefaults() {
        moodImages.clear(); loadDefaultImages();
        soundFiles.clear(); loadDefaultSounds();
        buttonIcons.clear();
        backgroundImageFile = null;
        soundEnabled = true; darkTheme = false; autoSave = false; fontSize = 14; nameColor = Color.BLACK; agingFactor = 1.0;
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, fontSize)); statsLabel.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        updateImage(); updateAllButtonIcons(); applyBackground(mainPanel); applyTheme(mainPanel); applyTheme(settingsPanel);
    }

    private void switchToSettings() {
        frame.setContentPane(settingsPanel);
        frame.revalidate(); frame.repaint();
        // Manually ensure the specific checkboxes are black when switching to settings
        if (autoSaveCheckBox != null) autoSaveCheckBox.setForeground(Color.BLACK);
        if (soundEnabledCheckBox != null) soundEnabledCheckBox.setForeground(Color.BLACK);
        if (themeCheckBox != null) themeCheckBox.setForeground(Color.BLACK);
    }

    private void switchToMain() {
        frame.setContentPane(mainPanel);
        applyBackground(mainPanel);
        applyTheme(mainPanel);
        updateImage(); updateAllButtonIcons();
        frame.revalidate(); frame.repaint();
    }

    private void saveSettings() {
        // simple explicit save example: write a tiny file when Save & Back pressed (or auto-save enabled)
        if (!autoSave) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("critter_settings.ser"))) {
            oos.writeObject(buttonIconsToPathMap());
            oos.writeObject(soundFilesToPathMap());
            oos.writeObject(backgroundImageFile == null ? null : backgroundImageFile.getAbsolutePath());
            oos.writeDouble(agingFactor);
            oos.writeBoolean(soundEnabled);
            oos.writeBoolean(darkTheme);
            oos.writeInt(fontSize);
        } catch (Exception ex) { /* ignore save errors */ }
    }

    private Map<String,String> buttonIconsToPathMap() {
        Map<String,String> m = new HashMap<>();
        for (Map.Entry<String, ImageIcon> e: buttonIcons.entrySet()) { m.put(e.getKey(), e.getValue().getDescription()); }
        return m;
    }
    private Map<String,String> soundFilesToPathMap() { Map<String,String> m = new HashMap<>(); for (Map.Entry<String, File> e: soundFiles.entrySet()) m.put(e.getKey(), e.getValue().getAbsolutePath()); return m; }

    /**
     * Applies the background image to the mainPanel with 50% transparency.
     * Only works if the container is a BackgroundPanel.
     */
    private void applyBackground(Container c) {
        if (!(c instanceof BackgroundPanel)) return; 
        BackgroundPanel bgPanel = (BackgroundPanel) c;

        if (backgroundImageFile==null) { 
            bgPanel.setBackgroundImage(null, 0, 0); 
            // Also ensure the root container has a default/theme color
            bgPanel.setOpaque(true);
        } else {
            bgPanel.setBackgroundImage(backgroundImageFile, frame.getWidth(), frame.getHeight());
            bgPanel.setOpaque(false); // Let the image paint
        }
    }
    
    /**
     * Applies dark/light theme to the container and its children.
     */
    private void applyTheme(Container c) {
        Color bg = darkTheme ? Color.DARK_GRAY : UIManager.getColor("Panel.background");
        Color fgWhite = Color.WHITE;
        Color fgBlack = Color.BLACK;

        // Apply background color
        if (backgroundImageFile == null) {
            c.setBackground(bg);
        }
        
        // Recursively set foreground color for all relevant components
        applyThemeRecursive(c, fgWhite, fgBlack);

        // Explicitly set the main display labels
        if (imgLabel != null) imgLabel.setForeground(darkTheme ? fgWhite : fgBlack);
        if (statusLabel != null) statusLabel.setForeground(darkTheme ? fgWhite : fgBlack);
        if (statsLabel != null) statsLabel.setForeground(darkTheme ? fgWhite : fgBlack);
        
        // Ensure the specific settings checkboxes are always black *after* the recursive theme applies.
        if (autoSaveCheckBox != null) autoSaveCheckBox.setForeground(Color.BLACK);
        if (soundEnabledCheckBox != null) soundEnabledCheckBox.setForeground(Color.BLACK);
        if (themeCheckBox != null) themeCheckBox.setForeground(Color.BLACK);
    }
    
    // fgWhite is for non-button text in dark mode (JLabel)
    // fgBlack is for button text (JButton)
    private void applyThemeRecursive(Container c, Color fgWhite, Color fgBlack) {
        for (Component comp : c.getComponents()) {
            // NOTE: We only handle JLabels here. JCheckBoxes are handled explicitly in applyTheme()
            if (comp instanceof JLabel) {
                // Change JLabels to white in dark mode, or black in light mode
                comp.setForeground(darkTheme ? fgWhite : fgBlack);
            } else if (comp instanceof JButton) {
                // Keep JButton text black/default.
                comp.setForeground(fgBlack); 
            } else if (comp instanceof Container) {
                // Recursively apply to children containers
                applyThemeRecursive((Container) comp, fgWhite, fgBlack);
            }
        }
    }


    private void doCheck() {
        statusLabel.setText("<html><center>" + critter.talk() + "</center></html>");
        playSound("click");
        critter.age();
        updateAfterAging();
    }

    private void postActionUpdate(String message) {
        statusLabel.setText("<html><center>" + message + "<br><br>" + critter.talk() + "</center></html>");
        // age considering agingFactor; simple approach: round to integer steps
        int steps = Math.max(1, (int)Math.round(agingFactor));
        for (int i=0;i<steps;i++) critter.age();
        updateAfterAging();
        if (autoSave) saveSettings();
    }

    private void updateAfterAging() {
        if (!critter.isAlive()) {
            statusLabel.setText("<html><center>💀 " + critter.getName() + " has become too weak or sad... The game is over.</center></html>");
        }
        updateImage();
        statsLabel.setText(getStatsHtml());
    }

    private String getStatsHtml() {
        return String.format("<html><center>Name: <b><font color='#%06x'>%s</font></b> &nbsp;&nbsp; Age: <b>%d</b><br>Fullness: %d &nbsp;&nbsp; Happiness: %d &nbsp;&nbsp; Cleanliness: %d &nbsp;&nbsp; Treats: %d</center></html>",
                nameColor.getRGB() & 0xFFFFFF, critter.getName(), critter.getAge(), critter.getFull(), critter.getHappy(), critter.getCleanliness(), critter.getTreats());
    }

    private void updateImage() {
        ImageIcon icon = selectMoodImage();
        if (icon != null) { 
            imgLabel.setIcon(icon); 
            imgLabel.setText(null); 
        } else { 
            // Fallback to ASCII text if no icon is found
            imgLabel.setIcon(null); 
            imgLabel.setText("<html><pre style='font-size:12px'>" + asciiForMood() + "</pre></html>"); 
        }
    }

    private ImageIcon selectMoodImage() {
        // Try to return the appropriate icon
        if (!critter.isAlive()) return firstAvailable("dead","sad","neutral");
        if (critter.isAsleep()) return firstAvailable("asleep","neutral");
        if (critter.getHappy() > 6 && critter.getFull() > 6 && critter.getCleanliness() > 6) return firstAvailable("happy","neutral");
        if (critter.getHappy() > 3) return firstAvailable("neutral","happy");
        if (critter.getHappy() > 0) return firstAvailable("sad","dirty","neutral");
        
        // Final fallback: return the best available mood image, or null if none exist.
        return firstAvailable("sad","dead","neutral");
    }

    private ImageIcon firstAvailable(String... keys) { 
        for (String k: keys) { 
            ImageIcon ic = moodImages.get(k); 
            if (ic!=null) return ic; 
        } 
        return null; 
    }

    private String asciiForMood() {
        if (!critter.isAlive()) return "( x _ x )\n /|   |\\\n  |   |\n / \\ / \\";
        if (critter.isAsleep()) return "( - _ - ) zZ\n /|   |\\\n  |   |\n / \\ / \\";
        if (critter.getHappy() > 6) return "( ^ ᴗ ^ )\n /|   |\\\n  |   |\n / \\ / \\";
        if (critter.getHappy() > 3) return "( ◕ ‿ ◕ )\n /|   |\\\n  |   |\n / \\ / \\";
        return "( > _ < )\n /|   |\\\n  |   |\n / \\ / \\";
    }

    private void playSound(String key) {
        if (!soundEnabled) return;
        File f = soundFiles.get(key);
        if (f==null) return;
        new Thread(() -> {
            try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(f)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                Thread.sleep(Math.min(clip.getMicrosecondLength()/1000, 4000));
                clip.close();
            } catch (Exception ex) { /* ignore sound errors */ }
        }).start();
    }
}
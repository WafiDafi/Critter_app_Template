# Critter_app_Template
A standalone virtual pet simulation game built in Java using the Swing framework. Features interactive critter care mechanics (feeding, playing, cleaning, sleeping), state-driven ASCII/image mood changes, sound effects playback, and an extensive, dynamically-rendered runtime Settings menu supporting user-customized features like background change)

# 🐾 Critter Pet Simulator (Java Swing Edition)

Welcome to **Critter Pet Simulator**! This is a standalone virtual pet game built from scratch using Java and the Swing framework. 

Beyond being a fun desktop toy, this project is designed from the ground up as an **educational sandbox**. If you are looking to understand how GUI (Graphical User Interface) applications work, or if you want to get your feet wet editing game mechanics, this codebase is the perfect, low-risk environment to start experimenting.

---

## 🚀 Why This Project is Useful for Learning

This repository strips away complex game engines (like Unity or Unreal) and uses raw Java. This makes it an excellent tool for developers looking to build concrete programming skills.

### 1. Understand Core Programming Concepts
By exploring the code, you will see real-world implementations of:
* **Object-Oriented Programming (OOP):** The code cleanly separates the pet's core business logic (`Critter.java`) from the visual interface rendering (`CritterGUIWithSettings.java`).
* **State Management:** Learn how a program tracks dynamic variables (like fullness, happiness, and cleanliness) and updates the application in real-time based on internal clocks and user actions.
* **File Handling & Serialization:** See how settings and game profiles can be saved locally onto a machine using Java's `Serializable` interface.

### 2. Learn GUI & Event-Driven Architecture
The project serves as a clear blueprint for Java Swing development:
* **Component Hierarchies:** Understand how nested panels, grid layouts, borders, and transparency layers are structured.
* **Event Listeners:** See exactly how a button click triggers custom sound effects, updates text fields, and shifts visual assets instantly.

---

## 🛠 How to Use This Project to Practice "Game Modding"

The best way to learn programming is by breaking and modifying things. Here are a few ways you can modify this code to familiarize yourself with editing game aspects:

### 💡 Level 1: Tweak the Game Balance (Easy)
Want to change the difficulty? Look inside the `Critter` class and modify the integers inside the core interaction loops:
* **Make the pet hungrier:** Find the `age()` method and change `full--;` to `full -= 2;`.
* **Change item power:** Find the `useTreat()` method and change how much `happy` or `full` points a treat gives.
* **Set hard limits:** Modify the `capStats()` function to change the maximum capacity of your pet's attributes.

### 🎨 Level 2: Add Custom Assets & Fallbacks (Medium)
The application dynamically checks local directories for custom images and audio files, falling back to procedural ASCII art if they are missing.
* You can change the default behaviors by altering the `loadDefaultImages()` or `loadDefaultSounds()` pathways.
* Try editing the `asciiForMood()` method to draw your own custom text-based creatures for different states!

### ⚔️ Level 3: Code New Mechanics (Advanced)
Ready to write new features? Use the existing codebase as a template to add:
* A new status metric (e.g., `Boredom` or `Energy`).
* A new button action (e.g., a "Train" or "Heal" mechanic).
* An automated background thread (`javax.swing.Timer`) so the pet ages automatically in real-time even if you don't click the "Check" button.

---

## 🏁 Quick Start

### Prerequisites
* **Java Development Kit (JDK) 8 or higher** installed on your machine.
* Any standard IDE (NetBeans, IntelliJ IDEA, Eclipse) or a command terminal.

### Running the App
1. Clone this repository to your local machine (Universal command):
   ```bash
   git clone [https://github.com/YOUR-USERNAME/YOUR-REPO-NAME.git](https://github.com/YOUR-USERNAME/YOUR-REPO-NAME.git)

2. Open the project folder in your preferred IDE.

3. Locate CritterApp2.java, right-click, and select Run File.

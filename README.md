# cybersapienttask

Android Exercise: Interactive Task Manager with Jetpack Compose Objective 

Build an Android application that serves as an interactive task manager, showcasing advanced
UI/UX skills with Jetpack Compose. 
The app should feature a visually appealing design, smooth animations, intuitive navigation, and accessibility support, all while adhering to Material Design 3 principles 
and the latest Android tools (Android SDK 34/35, Kotlin 2.x, Compose 1.6.x as of February 2025)


Loom video explainging everything  : https://www.loom.com/share/4ce2ca111f6d434b9b20eefe48df3ed5?sid=9340137b-30c7-4d01-9c6a-7f96a4cbe5fd 


# Task Manager

An interactive task manager Android application built with Jetpack Compose, featuring advanced UI/UX design, animations, and Material Design 3.

## Features

### Core Features
- **Task Creation**: Create tasks with title, description, priority level, and due date
- **Task List**: View tasks in a dynamic, filterable list with multiple sorting options
- **Task Details**: View task details and mark tasks as completed or delete them
- **Persistent Storage**: All tasks are saved locally using Room database

### Advanced UI Features
- **Drag-and-Drop**: Reorder tasks with a natural drag-and-drop interface with haptic feedback
- **Swipe Gestures**: Swipe right to complete tasks, swipe left to delete tasks with undo functionality
- **Custom Progress Indicator**: Visual circular progress bar showing completion percentage
- **Engaging Empty State**: Custom animated illustration and motivational message
- **Shimmer Loading Effect**: Placeholder animation while data loads

### UI/UX Design
- **Material Design 3**: Modern components following Material Design 3 guidelines
- **Dynamic Theming**: Support for light and dark themes with Material You color system
- **Custom Animations**:
    - Task addition/removal animations with slide-in/slide-out effects
    - Circular reveal animation for task details
    - Bounce effect on the FAB when tapped
- **Responsive Layout**: Adapts to different screen sizes and orientations

### Accessibility
- Full screen reader support with meaningful content descriptions
- Support for large text scaling
- Proper keyboard navigation
- Accessible touch targets

## Setup Instructions

1. **Clone the repository**:
   ```
   git clone https://github.com/yourusername/task-manager.git
   ```

2. **Open with Android Studio**:
    - Open Android Studio (Electric Eel or later)
    - Choose "Open an existing project"
    - Navigate to the cloned repository and select it

3. **Sync Gradle**:
    - Android Studio should automatically sync the Gradle files
    - If it doesn't, click "Sync Project with Gradle Files" in the toolbar

4. **Run the app**:
    - Connect an Android device or use an emulator (API 26 or higher)
    - Click the "Run" button in Android Studio

## Design Rationale

### Architecture

The application follows the MVVM (Model-View-ViewModel) architecture pattern:

- **Model**: Room database with Task entities and DAOs
- **View**: Jetpack Compose UI components
- **ViewModel**: Maintains UI state and business logic

This separation of concerns allows for better testability and maintainability.

### UI/UX Design Decisions

- **Bottom Navigation vs. Drawer**: Opted for a simple navigation structure without bottom tabs or navigation drawer to keep the app focused and intuitive.
- **FAB with Animation**: The floating action button has a bounce effect to make it more playful and provide visual feedback.
- **Task Card Design**: Task cards show the most important information at a glance, with clear hierarchy:
    - Title in larger text
    - Description as secondary information
    - Visual priority indicator for quick recognition
    - Completion checkbox accessible from the list

### Performance Optimizations

- **LazyColumn**: Used for efficient list rendering with minimal recompositions
- **Remember and MutableState**: Strategic use of remember and state to minimize unnecessary recompositions
- **Key-based Recomposition**: List items use keys for stable identity
- **LaunchedEffect**: Used for side effects to prevent leaks and unnecessary executions

### Animations

Animations were carefully chosen to:
- Enhance the user experience without being distracting
- Provide visual feedback for actions
- Guide the user's attention to important elements
- Make the app feel more responsive and polished

## Testing

- **UI Tests**: Verify main user flows like task creation and filtering
- **Screenshot Tests**: Ensure consistent appearance across themes
- **Accessibility Testing**: Verify screen reader compatibility

## Future Improvements

- Add task categorization with tags or folders
- Implement task reminders with notifications
- Add cloud synchronization for tasks
- Support for recurring tasks
- Add statistics and insights about task completion
- Implement widget for home screen










# Accessibility Testing Strategy

This document outlines the approach for testing accessibility features in the Task Manager app, focusing on screenshot tests to validate UI across various accessibility configurations.

## Testing Accessibility Configurations

### Configuration Matrix

Test each key screen across the following configuration matrix:

| Theme | Text Size | High Contrast | Language Direction |
|-------|-----------|---------------|-------------------|
| Light | Standard  | Off           | LTR               |
| Light | Standard  | On            | LTR               |
| Light | Large     | Off           | LTR               |
| Light | Large     | On            | LTR               |
| Dark  | Standard  | Off           | LTR               |
| Dark  | Standard  | On            | LTR               |
| Dark  | Large     | Off           | LTR               |
| Dark  | Large     | On            | LTR               |

### Key Screens for Testing

1. Task List (empty state)
2. Task List (with tasks)
3. Task Creation
4. Task Details
5. Settings
6. Dialogs/Confirmations

## Screenshot Testing Implementation

### Base Test Framework

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AccessibilityScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    
    // Test configurations
    private val configurations = listOf(
        AccessibilityConfig(darkTheme = false, textScale = 1.0f, highContrast = false),
        AccessibilityConfig(darkTheme = false, textScale = 1.0f, highContrast = true),
        AccessibilityConfig(darkTheme = false, textScale = 1.3f, highContrast = false),
        AccessibilityConfig(darkTheme = false, textScale = 1.3f, highContrast = true),
        AccessibilityConfig(darkTheme = true, textScale = 1.0f, highContrast = false),
        AccessibilityConfig(darkTheme = true, textScale = 1.0f, highContrast = true),
        AccessibilityConfig(darkTheme = true, textScale = 1.3f, highContrast = false),
        AccessibilityConfig(darkTheme = true, textScale = 1.3f, highContrast = true)
    )
    
    @Test
    fun testTaskListAccessibility() {
        configurations.forEach { config ->
            testScreenWithConfig("task_list", config) {
                TaskListScreen(
                    viewModel = getMockViewModel(),
                    onTaskClick = {},
                    onAddTask = {},
                    onSettingsClick = {}
                )
            }
        }
    }
    
    // Helper method to render and capture a screenshot with the specified configuration
    private fun testScreenWithConfig(
        screenName: String,
        config: AccessibilityConfig,
        content: @Composable () -> Unit
    ) {
        // Configure and render the screen
        composeTestRule.setContent {
            CompositionLocalProvider(LocalTextScaleFactor provides config.textScale) {
                TaskManagerTheme(
                    darkTheme = config.darkTheme,
                    highContrastMode = config.highContrast
                ) {
                    content()
                }
            }
        }
        
        // Capture screenshot
        val configSuffix = "${if (config.darkTheme) "dark" else "light"}_" +
                           "${if (config.highContrast) "contrast" else "normal"}_" +
                           "scale${config.textScale}"
        takeScreenshot("${screenName}_${configSuffix}")
    }
    
    // Helper to capture screenshots
    private fun takeScreenshot(name: String) {
        val directory = File(context.filesDir, "accessibility_screenshots")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        val screenshotFile = File(directory, "$name.png")
        composeTestRule.onRoot().captureToImage().asAndroidBitmap().compress(
            android.graphics.Bitmap.CompressFormat.PNG,
            100,
            screenshotFile.outputStream()
        )
    }
    
    // Data class for test configurations
    data class AccessibilityConfig(
        val darkTheme: Boolean,
        val textScale: Float,
        val highContrast: Boolean
    )
}
```

## Visual Validation Criteria

For each screenshot, validate:

1. **Text Readability**: All text must be clearly readable
2. **Color Contrast**: Elements should meet contrast guidelines:
    - Normal mode: WCAG AA (4.5:1 for normal text, 3:1 for large text)
    - High contrast mode: WCAG AAA (7:1 for normal text, 4.5:1 for large text)
3. **Touch Target Size**: Interactive elements must maintain adequate touch target size (at least 48x48dp)
4. **Layout Integrity**: No overlapping elements or text truncation
5. **Focus Indicators**: Clear focus indicators on interactive elements

## Automated Analysis

Use accessibility analysis tools on the screenshots:

```kotlin
@Test
fun verifyContrastRatios() {
    configurations.forEach { config ->
        val screenshot = loadScreenshot("task_list", config)
        val analysis = AccessibilityAnalyzer.analyzeContrast(screenshot)
        
        // For high contrast mode, use stricter requirements
        val minRatio = if (config.highContrast) 7.0f else 4.5f
        
        analysis.textElements.forEach { element ->
            assertTrue(
                "Text element at ${element.bounds} has insufficient contrast ratio (${element.contrastRatio} < $minRatio)",
                element.contrastRatio >= minRatio
            )
        }
    }
}
```

## Manual Testing Checklist

In addition to automated testing, perform these manual checks:

- [ ] Verify TalkBack navigation flows logically through all screens
- [ ] Test with actual external keyboard to confirm keyboard navigation
- [ ] Verify all interactions can be completed without touch input
- [ ] Test with Switch Access for minimum motor ability users
- [ ] Verify content scaling with system text size changes
- [ ] Test color correctness with color blindness simulation tools

## Regression Prevention

Screenshots should be generated as part of the CI/CD pipeline and compared against baseline images to detect accessibility regressions before release.

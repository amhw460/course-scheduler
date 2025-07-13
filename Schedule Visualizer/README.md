# Schedule Visualizer

A modern, visually appealing course schedule visualizer built in Java with enhanced graphics and user experience.

## Features

### Visual Enhancements
- **Modern Color Palette**: Beautiful gradient colors for course blocks with automatic color assignment based on course ID
- **Anti-aliasing**: Smooth, crisp graphics with enabled anti-aliasing for both shapes and text
- **Gradient Effects**: Course blocks feature vertical gradients for a modern, professional look
- **Drop Shadows**: Subtle shadows on course blocks for depth and visual hierarchy
- **Rounded Corners**: All UI elements use rounded corners for a modern appearance
- **Professional Typography**: Uses Segoe UI font family for clean, readable text

### Layout Improvements
- **Title Header**: Prominent "Course Schedule" title with shadow effect
- **Day Headers**: Clear day-of-week labels with styled backgrounds
- **Time Labels**: Hour markers on the left side for easy time reference
- **Grid Lines**: Subtle horizontal lines to separate time periods
- **Responsive Design**: Adapts to different window sizes

### Information Display
- **Course Information**: Displays course ID prominently in each block
- **Time Details**: Shows start and end times for each course session
- **Instructor Information**: Displays instructor names when space permits
- **Color Coding**: Each course gets a unique color based on its ID hash

### Technical Improvements
- **Enhanced Graphics2D**: Full utilization of Java's advanced graphics capabilities
- **Better Memory Management**: Optimized rendering for smooth performance
- **Clean Code Structure**: Well-organized, maintainable codebase

## Usage

1. Ensure you have the `output.json` file in the same directory as the executable
2. Run the application: `java -cp "lib/*;src" VisualizerDriver`
3. The schedule will be displayed in a modern, visually appealing interface

## Requirements

- Java 8 or higher
- JSON library (included in lib directory)
- `output.json` file with course schedule data

## File Structure

```
Schedule Visualizer/
├── src/
│   ├── Timing.java           # Enhanced graphics and time calculations
│   ├── ScheduleBlock.java    # Course block data and utilities
│   ├── Visualization.java    # Main visualization component
│   └── VisualizerDriver.java # Application entry point
├── lib/                      # Dependencies
└── output.json              # Course schedule data
```

## Visual Design Philosophy

The visualizer follows modern design principles:
- **Clean and Minimal**: Focus on readability and information hierarchy
- **Consistent Color Scheme**: Professional color palette with good contrast
- **Visual Hierarchy**: Clear distinction between different information levels
- **Accessibility**: High contrast text and clear visual separation
- **Modern Aesthetics**: Rounded corners, gradients, and shadows for contemporary look

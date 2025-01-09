#ifndef ENTITY_H
#define ENTITY_H
#include <cstdint> // For uint8_t type

// Define the Color struct
struct Color {
    uint8_t r, g, b, a; // Red, Green, Blue, Alpha values
};

class Entity{

private:
int width;
int height;
Color color;
void draw();

public:
// Constructor 
Entity(int w, int h, Color c);
// Getters and setters for color
int getWidth() const;
void setWidth(int width);
// Getters and setters for color
int getHeight() const;
void setHeight(int height);

// Getters and setters for color
Color getColor() const;
void setColor(const Color& color);

};
#endif

#include <iostream>
#include "Entity.h"

Entity::Entity(int w, int h, Color c){
    setWidth(w);
    setHeight(h);
    setColor(c);
}

// Width methods
int Entity::getWidth() const{ 
    return width;
}

void Entity::setWidth(int w) {
    width = w;
}

// Height methods
int Entity::getHeight() const{ 
    return height;
}

void Entity::setHeight(int h) {
    height = h;
}

// Color methods
Color Entity::getColor() const {
    return color;
}

void Entity::setColor(const Color& c) {
    color = c;
}

/*
@param SDL_Renderer is a render object in SDL2 which responds to 2D graphics to the screen

*/
void Entity::draw(SDL_Renderer* renderer) {
    if (!renderer) {
        std::cerr << "Failed to create renderer: " << SDL_GetError() << std::endl;
        return;
    }

    // Draw a red circle (generic entity)
    int centerX = 512; // Example center
    int centerY = 960; // Example center
    int radius = width / 2; // Use width as diameter

    SDL_SetRenderDrawColor(renderer, 255, 0, 0, 255);
    for (int w = 0; w < radius * 2; w++) {
        for (int h = 0; h < radius * 2; h++) {
            int dx = radius - w; // Horizontal offset
            int dy = radius - h; // Vertical offset
            if ((dx * dx + dy * dy) <= (radius * radius)) {
                SDL_RenderDrawPoint(renderer, centerX + dx, centerY + dy);
            }
        }
    }
}

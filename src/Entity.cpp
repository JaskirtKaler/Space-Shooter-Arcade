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
void Entity::draw(SDL_Renderer* renderer){
    if(!renderer){
        std::cerr << "Failed to create renderer: "<< SDL_GetError() << std::endl;
        
        // default drawing for any entity
        SDL_SetRenderDrawColor(renderer, 255, 0, 0, 255);
        SDL_Rect defaultRect = {100, 100, width, height}; // position and size 
        SDL_RenderFillRect(renderer, &defaultRect);
        return;
    }else{
        SDL_SetRenderDrawColor(renderer, color.r, color.g, color.b, color.a);
        SDL_Rect rect = {100, 100, width, height}; // Default position and size
        SDL_RenderFillRect(renderer, &rect);    
    }
}
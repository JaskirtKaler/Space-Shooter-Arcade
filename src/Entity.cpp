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

// draw Entity
void Entity::draw(){

}
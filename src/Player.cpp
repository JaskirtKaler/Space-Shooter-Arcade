#include "Player.h"
#include <iostream>

// Constructor
Player::Player(int w, int h, const std::string& spritePath, SDL_Renderer* renderer)
    : Entity(w, h, {255, 255, 255, 255}), texture(nullptr) {
    SDL_Surface* surface = IMG_Load(spritePath.c_str());
    if (!surface) {
        std::cerr << "Failed to load sprite: " << IMG_GetError() << std::endl;
    } else {
        texture = SDL_CreateTextureFromSurface(renderer, surface);
        SDL_FreeSurface(surface);
        if (!texture) {
            std::cerr << "Failed to create texture: " << SDL_GetError() << std::endl;
        }
    }
}

// Destructor
Player::~Player() {
    if (texture) {
        SDL_DestroyTexture(texture);
    }
}

// Override draw method
void Player::draw(SDL_Renderer* renderer) {
    if (texture) {
        SDL_Rect rect = {100, 100, getWidth(), getHeight()}; // Position and size
        SDL_RenderCopy(renderer, texture, nullptr, &rect);
    } else {
        // Fallback to default draw if texture is missing
        Entity::draw(renderer);
    }
}

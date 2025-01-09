#include "Player.h"
#include <iostream>

// Constructor
// Player::Player(int w, int h, const std::string& spritePath, SDL_Renderer* renderer)
//     : Entity(w, h, {255, 255, 255, 255}), texture(nullptr) {
//     SDL_Surface* surface = IMG_Load(spritePath.c_str());
//     if (!surface) {
//         std::cerr << "Failed to load sprite: " << IMG_GetError() << std::endl;
//     } else {
//         texture = SDL_CreateTextureFromSurface(renderer, surface);
//         SDL_FreeSurface(surface);
//         if (!texture) {
//             std::cerr << "Failed to create texture: " << SDL_GetError() << std::endl;
//         }
//     }
// }
Player::Player(int w, int h, const std::string& spritePath, SDL_Renderer* renderer)
    : Entity(w, h, {255, 0, 0, 255}) {
    // The constructor now initializes the Entity with default red color
    // No texture is used or loaded.
}


// Destructor
Player::~Player() {
    if (texture) {
        SDL_DestroyTexture(texture);
    }
}

// Override draw method
void Player::draw(SDL_Renderer* renderer) {
    static bool logged = false; // Only log once per run
    if (!logged) {
        printf("Entering player draw method.\n");
        logged = true;
    }

    if (!renderer) {
        std::cerr << "Renderer is null: " << SDL_GetError() << std::endl;
        return;
    }

    // Set the drawing color to red
    SDL_SetRenderDrawColor(renderer, 255, 0, 0, 255);

    // Define the vertices for the triangle
    SDL_Point vertices[3] = {
        {512, 960},         // Bottom-middle
        {512 - 50, 960 - 100}, // Top-left
        {512 + 50, 960 - 100}  // Top-right
    };

    // Draw the filled triangle (naive approach)
    for (int y = vertices[1].y; y <= vertices[0].y; ++y) {
        int x1 = vertices[1].x + (y - vertices[1].y) * (vertices[0].x - vertices[1].x) / (vertices[0].y - vertices[1].y);
        int x2 = vertices[2].x + (y - vertices[2].y) * (vertices[0].x - vertices[2].x) / (vertices[0].y - vertices[2].y);

        if (x1 > x2) std::swap(x1, x2);

        for (int x = x1; x <= x2; ++x) {
            SDL_RenderDrawPoint(renderer, x, y);
        }
    }
}



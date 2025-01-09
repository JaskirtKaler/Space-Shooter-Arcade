#ifndef PLAYER_H
#define PLAYER_H

#include "Entity.h"
#include <string>

class Player : public Entity {
private:
    SDL_Texture* texture;

public:
    // Constructor
    Player(int w, int h, const std::string& spritePath, SDL_Renderer* renderer);

    // Destructor
    ~Player();

    // Override draw method
    void draw(SDL_Renderer* renderer) override;
};

#endif

#include <iostream>
#include "GameBoard.h"


GameBoard::GameBoard(){
}

int GameBoard::getWidth() const{
    return Width;
}

void GameBoard::setWidth(int w){
    Width = w;
}

int GameBoard::getHeight() const{
    return Height;
}

void GameBoard::setHeight(int h){
    Height = h;
}



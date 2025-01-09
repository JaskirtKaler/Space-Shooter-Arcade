#include <iostream>
#include "GameBoard.h"


GameBoard::GameBoard(){
    Width = 1024;
    Height = 1200;
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



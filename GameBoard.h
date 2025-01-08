#ifndef GAMEBOARD_H
#define GAMEBOARD_H
#include <vector>

class GameBoard{

private:
int Width;
int Height;

public:
GameBoard();
int getWidth() const;
void setWidth(int width);
int getHeight() const;
void setHeight(int height);


};


#endif

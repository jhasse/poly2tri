#!/usr/bin/env python2.6
from framework import Game
 
class Poly2Tri(Game):

    #Screen size
    screen_size = 800.0, 600.0

    def __init__(self):
        super(Poly2Tri, self).__init__(*self.screen_size)
        self.main_loop()
        
    def update(self):
        pass
        
    def render(self):
        pass
        
if __name__ == '__main__':
    demo = Poly2Tri()
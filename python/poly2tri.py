#!/usr/bin/env python2.6
from framework import Game, draw_polygon, reset_zoom, draw_line

from seidel import Triangulator

class Poly2Tri(Game):

    screen_size = 800.0, 600.0

    def __init__(self):
        super(Poly2Tri, self).__init__(*self.screen_size)       
        
        # Load point set
        file_name = "../data/star.dat"
        self.points = self.load_points(file_name)
        
        # Triangulate
        t1 = self.time
        seidel = Triangulator(self.points)
        dt = (self.time - t1) * 1000.0
        
        self.triangles = seidel.triangles()
        #self.trapezoids = seidel.trapezoids
        self.trapezoids = seidel.trapezoidal_map.map
        self.edges = seidel.edge_list
        print "time (ms) = %f  , num triangles = %d" % (dt, len(self.triangles))
            
        self.main_loop()
        
    def update(self):
        pass
        
    def render(self):
        reset_zoom(2.1, (400, 100), self.screen_size)
        red = 255, 0, 0
        yellow = 255, 255, 0
        green = 0, 255, 0
        for t in self.triangles:
            draw_polygon(t, red)
        
        '''
        for t in self.trapezoids:
            verts = self.trapezoids[t].vertices()
            #verts = t.vertices()
            draw_polygon(verts, yellow)
        '''
        for e in self.edges:
            p1 = e.p.x, e.p.y
            p2 = e.q.x, e.q.y
            draw_line(p1, p2, green)

        #draw_polygon(self.points, green)
        
        
    def load_points(self, file_name):
        infile = open(file_name, "r")
        points = []
        while infile:
            line = infile.readline()
            s = line.split()
            if len(s) == 0:
                break
            points.append((float(s[0]), float(s[1])))
        return points

spam = (544.80998999999997, 579.86046999999996), (544.80998999999997, 450.57477), (594.09569999999997, 450.57477), (643.38142000000005, 450.57477), (643.38142000000005, 525.26486999999997), (643.38142000000005, 599.95487000000003), (603.67391999999995, 654.55056999999999), (563.96655999999996, 709.14621999999997), (554.38819999999998, 709.14621999999997), (544.80998999999997, 709.14621999999997)
eggs = [474.80999000000003, 555.15656999999999], [474.80999000000003, 530.87086999999997], [509.09570000000002, 530.87086999999997], [543.38142000000005, 530.87086999999997], [543.38142000000005, 555.15656999999999], [543.38142000000005, 579.44227000000001], [509.09570000000002, 579.44227000000001], [474.80999000000003, 579.44227000000001]   

if __name__ == '__main__':
    demo = Poly2Tri()
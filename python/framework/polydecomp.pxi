##
## Ported from PolyDeomp by Mark Bayazit 
## http://mnbayazit.com/406/bayazit
##
from sys import float_info

def makeCCW(list poly):
    cdef int br = 0
    # find bottom right point
    for i from 1 <= i < len(poly):
        if poly[i][1] < poly[br][1] or (poly[i][1] == poly[br][1] and poly[i][0] > poly[br][0]):
            br = i
    # reverse poly if clockwise
    if not left(at(poly, br - 1), at(poly, br), at(poly, br + 1)):
        poly.reverse()

cpdef list decompose_poly(list poly, list polys):

    cdef list upperInt = [], lowerInt = [], p = [], closestVert = []
    cdef float upperDist, lowerDist, d, closestDist
    cdef int upper_index, lower_index, closest_index
    cdef list lower_poly = [], upper_poly = []

    for i from 0 <= i < len(poly):
        if is_reflex(poly, i):
            upperDist = lowerDist = float_info.max
            for j from 0 <= j < len(poly):
                if left(at(poly, i - 1), at(poly, i), at(poly, j)) and rightOn(at(poly, i - 1), at(poly, i), at(poly, j - 1)):
                    # if line intersects with an edge
                    # find the point of intersection
                    p = intersection(at(poly, i - 1), at(poly, i), at(poly, j), at(poly, j - 1)) 
                    if right(at(poly, i + 1), at(poly, i), p): 
                        # make sure it's inside the poly
                        d = sqdist(poly[i], p)
                        if d < lowerDist: 
                            # keep only the closest intersection
                            lowerDist = d
                            lowerInt = p
                            lower_index = j
                if left(at(poly, i + 1), at(poly, i), at(poly, j + 1)) and rightOn(at(poly, i + 1), at(poly, i), at(poly, j)):
                    p = intersection(at(poly, i + 1), at(poly, i), at(poly, j), at(poly, j + 1))
                    if left(at(poly, i - 1), at(poly, i), p):
                        d = sqdist(poly[i], p)
                        if d < upperDist:
                            upperDist = d
                            upperInt = p
                            upper_index = j

            # if there are no vertices to connect to, choose a point in the middle
            if lower_index == (upper_index + 1) % len(poly):
                p[0] = (lowerInt[0] + upperInt[0]) / 2
                p[1] = (lowerInt[1] + upperInt[1]) / 2

                if i < upper_index:
                    lower_poly.extend(poly[i:upper_index+1])
                    lower_poly.append(p)
                    upper_poly.append(p)
                    if lower_index != 0:
                        upper_poly.extend(poly[lower_index:])
                    upper_poly.extend(poly[:i+1])
                else:
                    if i != 0: 
                        lower_poly.extend(poly[i:])
                    lower_poly.extend(poly[:upper_index+1])
                    lower_poly.append(p)
                    upper_poly.append(p)
                    upper_poly.extend(poly[lower_index:i+1])
            else:
            
                # connect to the closest point within the triangle

                if lower_index > upper_index:
                    upper_index += len(poly)
                
                closestDist = float_info.max
                for j from lower_index <= j <= upper_index:
                    if leftOn(at(poly, i - 1), at(poly, i), at(poly, j)) and rightOn(at(poly, i + 1), at(poly, i), at(poly, j)):
                        d = sqdist(at(poly, i), at(poly, j))
                        if d < closestDist:
                            closestDist = d
                            closestVert = at(poly, j)
                            closest_index = j % len(poly)

                if i < closest_index:
                    lower_poly.extend(poly[i:closest_index+1])
                    if closest_index != 0: 
                        upper_poly.extend(poly[closest_index:])
                    upper_poly.extend(poly[:i+1])
                else:
                    if i != 0:
                        lower_poly.extend(poly[i:])
                    lower_poly.extend(poly[:closest_index+1])
                    upper_poly.extend(poly[closest_index:i+1])
  
            # solve smallest poly first
            if len(lower_poly) < len(upper_poly):
                decompose_poly(lower_poly, polys)
                decompose_poly(upper_poly, polys)
            else:
                decompose_poly(upper_poly, polys)
                decompose_poly(lower_poly, polys)
            
            return

    polys.append(poly)
    
cdef list intersection(list p1, list p2, list q1, list q2):
    cdef list i = []
    cdef float a1, b1, c1, a2, b2, c2, det
    a1 = p2[1] - p1[1]
    b1 = p1[0] - p2[0]
    c1 = a1 * p1[0] + b1 * p1[1]
    a2 = q2[1] - q1[1]
    b2 = q1[0] - q2[0]
    c2 = a2 * q1[0] + b2 * q1[1]
    det = a1 * b2 - a2 * b1
    if not eq(det, 0): 
        # lines are not parallel
        i.append((b2 * c1 - b1 * c2) / det)
        i.append((a1 * c2 - a2 * c1) / det)
    return i

cdef bool eq(float a, float b):
    return abs(a - b) <= 1e-8

cdef list at(list v, int i):
    return v[i%len(v)]
    
cdef float area(list a, list b, list c):
    return (((b[0] - a[0])*(c[1] - a[1]))-((c[0] - a[0])*(b[1] - a[1])))

cdef bool left(list a, list b, list c):
    return area(a, b, c) > 0

cdef bool leftOn(list a, list b, list c):
    return area(a, b, c) >= 0

cdef bool right(list a, list b, list c):
    return area(a, b, c) < 0

cdef bool rightOn(list a, list b, list c):
    return area(a, b, c) <= 0

cdef float sqdist(list a, list b):
    cdef float dx = b[0] - a[0]
    cdef float dy = b[1] - a[1]
    return dx * dx + dy * dy
    
cdef bool is_reflex(list poly, int i):
    return right(at(poly, i - 1), at(poly, i), at(poly, i + 1))
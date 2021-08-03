/*
 * Poly2Tri Copyright (c) 2009-2018, Poly2Tri Contributors
 * https://github.com/jhasse/poly2tri
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Poly2Tri nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// Include guard
#ifndef SHAPES_H
#define SHAPES_H

#if defined(_WIN32)
# define COMPILER_DLLEXPORT __declspec(dllexport)
# define COMPILER_DLLIMPORT __declspec(dllimport)
#else
# define COMPILER_DLLEXPORT __attribute__ ((visibility ("default")))
# define COMPILER_DLLIMPORT __attribute__ ((visibility ("default")))
#endif

#ifndef P2T_EXPORT
#  if defined(P2T_DLL_EXPORTS) && defined(P2T_DLL_IMPORTS)
#    error "Only P2T_DLL_EXPORTS or P2T_DLL_IMPORTS can be defined, not both."
#  elif defined(P2T_DLL_EXPORTS)
#    define P2T_EXPORT COMPILER_DLLEXPORT
#  elif defined(P2T_DLL_IMPORTS)
#    define P2T_EXPORT COMPILER_DLLIMPORT
#  else
#    define P2T_EXPORT
#  endif
#endif

#include <cmath>
#include <cstddef>
#include <stdexcept>
#include <vector>

namespace p2t {

struct Edge;

struct Point {

  double x, y;

  /// Default constructor does nothing (for performance).
  P2T_EXPORT Point()
  {
    x = 0.0;
    y = 0.0;
  }

  /// The edges this point constitutes an upper ending point
  std::vector<Edge*> edge_list;

  /// Construct using coordinates.
  P2T_EXPORT Point(double x, double y) : x(x), y(y) {}

  /// Set this point to all zeros.
  P2T_EXPORT void set_zero()
  {
    x = 0.0;
    y = 0.0;
  }

  /// Set this point to some specified coordinates.
  P2T_EXPORT void set(double x_, double y_)
  {
    x = x_;
    y = y_;
  }

  /// Negate this point.
  P2T_EXPORT Point operator -() const
  {
    Point v;
    v.set(-x, -y);
    return v;
  }

  /// Add a point to this point.
  P2T_EXPORT void operator +=(const Point& v)
  {
    x += v.x;
    y += v.y;
  }

  /// Subtract a point from this point.
  P2T_EXPORT void operator -=(const Point& v)
  {
    x -= v.x;
    y -= v.y;
  }

  /// Multiply this point by a scalar.
  P2T_EXPORT void operator *=(double a)
  {
    x *= a;
    y *= a;
  }

  /// Get the length of this point (the norm).
  P2T_EXPORT double Length() const
  {
    return sqrt(x * x + y * y);
  }

  /// Convert this point into a unit point. Returns the Length.
  P2T_EXPORT double Normalize()
  {
    const double len = Length();
    x /= len;
    y /= len;
    return len;
  }

};

std::ostream& operator<<(std::ostream&, const Point&);

// Represents a simple polygon's edge
struct Edge {

  Point* p, *q;

  /// Constructor
  P2T_EXPORT Edge(Point& p1, Point& p2) : p(&p1), q(&p2)
  {
    if (p1.y > p2.y) {
      q = &p1;
      p = &p2;
    } else if (p1.y == p2.y) {
      if (p1.x > p2.x) {
        q = &p1;
        p = &p2;
      } else if (p1.x == p2.x) {
        // Repeat points
        throw std::runtime_error("Edge::Edge: p1 == p2");
      }
    }

    q->edge_list.push_back(this);
  }
};

// Triangle-based data structures are know to have better performance than quad-edge structures
// See: J. Shewchuk, "Triangle: Engineering a 2D Quality Mesh Generator and Delaunay Triangulator"
//      "Triangulations in CGAL"
class Triangle {
public:

/// Constructor
P2T_EXPORT Triangle(Point& a, Point& b, Point& c);

/// Flags to determine if an edge is a Constrained edge
bool constrained_edge[3];
/// Flags to determine if an edge is a Delauney edge
bool delaunay_edge[3];

P2T_EXPORT Point* GetPoint(int index);
P2T_EXPORT Point* PointCW(const Point& point);
P2T_EXPORT Point* PointCCW(const Point& point);
P2T_EXPORT Point* OppositePoint(Triangle& t, const Point& p);

P2T_EXPORT Triangle* GetNeighbor(int index);
P2T_EXPORT void MarkNeighbor(Point* p1, Point* p2, Triangle* t);
P2T_EXPORT void MarkNeighbor(Triangle& t);

P2T_EXPORT void MarkConstrainedEdge(int index);
P2T_EXPORT void MarkConstrainedEdge(Edge& edge);
P2T_EXPORT void MarkConstrainedEdge(Point* p, Point* q);

P2T_EXPORT int Index(const Point* p);
P2T_EXPORT int EdgeIndex(const Point* p1, const Point* p2);

P2T_EXPORT Triangle* NeighborAcross(const Point& point);
P2T_EXPORT Triangle* NeighborCW(const Point& point);
P2T_EXPORT Triangle* NeighborCCW(const Point& point);
P2T_EXPORT bool GetConstrainedEdgeCCW(const Point& p);
P2T_EXPORT bool GetConstrainedEdgeCW(const Point& p);
P2T_EXPORT void SetConstrainedEdgeCCW(const Point& p, bool ce);
P2T_EXPORT void SetConstrainedEdgeCW(const Point& p, bool ce);
P2T_EXPORT bool GetDelunayEdgeCCW(const Point& p);
P2T_EXPORT bool GetDelunayEdgeCW(const Point& p);
P2T_EXPORT void SetDelunayEdgeCCW(const Point& p, bool e);
P2T_EXPORT void SetDelunayEdgeCW(const Point& p, bool e);

P2T_EXPORT bool Contains(const Point* p);
P2T_EXPORT bool Contains(const Edge& e);
P2T_EXPORT bool Contains(const Point* p, const Point* q);
P2T_EXPORT void Legalize(Point& point);
P2T_EXPORT void Legalize(Point& opoint, Point& npoint);
/**
 * Clears all references to all other triangles and points
 */
P2T_EXPORT void Clear();
P2T_EXPORT void ClearNeighbor(const Triangle *triangle);
P2T_EXPORT void ClearNeighbors();
P2T_EXPORT void ClearDelunayEdges();

P2T_EXPORT inline bool IsInterior();
P2T_EXPORT inline void IsInterior(bool b);

P2T_EXPORT void DebugPrint();

P2T_EXPORT bool CircumcicleContains(const Point&) const;

private:

P2T_EXPORT bool IsCounterClockwise() const;

/// Triangle points
Point* points_[3];
/// Neighbor list
Triangle* neighbors_[3];

/// Has this triangle been marked as an interior triangle?
bool interior_;
};

inline bool cmp(const Point* a, const Point* b)
{
  if (a->y < b->y) {
    return true;
  } else if (a->y == b->y) {
    // Make sure q is point with greater x value
    if (a->x < b->x) {
      return true;
    }
  }
  return false;
}

/// Add two points_ component-wise.
inline Point operator +(const Point& a, const Point& b)
{
  return Point(a.x + b.x, a.y + b.y);
}

/// Subtract two points_ component-wise.
inline Point operator -(const Point& a, const Point& b)
{
  return Point(a.x - b.x, a.y - b.y);
}

/// Multiply point by scalar
inline Point operator *(double s, const Point& a)
{
  return Point(s * a.x, s * a.y);
}

inline bool operator ==(const Point& a, const Point& b)
{
  return a.x == b.x && a.y == b.y;
}

inline bool operator !=(const Point& a, const Point& b)
{
  return !(a.x == b.x) || !(a.y == b.y);
}

/// Peform the dot product on two vectors.
inline double Dot(const Point& a, const Point& b)
{
  return a.x * b.x + a.y * b.y;
}

/// Perform the cross product on two vectors. In 2D this produces a scalar.
inline double Cross(const Point& a, const Point& b)
{
  return a.x * b.y - a.y * b.x;
}

/// Perform the cross product on a point and a scalar. In 2D this produces
/// a point.
inline Point Cross(const Point& a, double s)
{
  return Point(s * a.y, -s * a.x);
}

/// Perform the cross product on a scalar and a point. In 2D this produces
/// a point.
inline Point Cross(double s, const Point& a)
{
  return Point(-s * a.y, s * a.x);
}

inline Point* Triangle::GetPoint(int index)
{
  return points_[index];
}

inline Triangle* Triangle::GetNeighbor(int index)
{
  return neighbors_[index];
}

inline bool Triangle::Contains(const Point* p)
{
  return p == points_[0] || p == points_[1] || p == points_[2];
}

inline bool Triangle::Contains(const Edge& e)
{
  return Contains(e.p) && Contains(e.q);
}

inline bool Triangle::Contains(const Point* p, const Point* q)
{
  return Contains(p) && Contains(q);
}

inline bool Triangle::IsInterior()
{
  return interior_;
}

inline void Triangle::IsInterior(bool b)
{
  interior_ = b;
}

/// Is this set a valid delaunay triangulation?
bool IsDelaunay(const std::vector<p2t::Triangle*>&);

}

#endif

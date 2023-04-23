/*
 * Poly2Tri Copyright (c) 2009-2022, Poly2Tri Contributors
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
#include <poly2tri/poly2tri.h>

#include <GLFW/glfw3.h>

#include <algorithm>
#include <cassert>
#include <cstdlib>
#include <ctime>
#include <exception>
#include <fstream>
#include <iostream>
#include <iterator>
#include <limits>
#include <list>
#include <numeric>
#include <sstream>
#include <string>
#include <utility>
#include <vector>

using namespace std;
using namespace p2t;

bool ParseFile(string filename, vector<Point>& out_polyline, vector<vector<Point>>& out_holes,
               vector<Point>& out_steiner);
std::pair<Point, Point> BoundingBox(gsl::span<const Point> polyline);
void GenerateRandomPointDistribution(size_t num_points, double min, double max,
                                     vector<Point>& out_polyline,
                                     vector<vector<Point>>& out_holes,
                                     vector<Point>& out_steiner);
void Init(int window_width, int window_height);
void ShutDown(int return_code);
void MainLoop(const double zoom);
void Draw(const double zoom);
void DrawMap(const double zoom);
void ConstrainedColor(bool constrain);
double StringToDouble(const std::string& s);
double Random(double (*fun)(double), double xmin, double xmax);
double Fun(double x);

double rotate_y = 0.0,
       rotate_z = 0.0;
const double rotations_per_tick = 0.2;

/// Default window size
constexpr int default_window_width = 800;
constexpr int default_window_height = 600;

/// Autozoom border (percentage)
const double autozoom_border = 0.05;

/// Screen center x
double cx = 0.0;
/// Screen center y
double cy = 0.0;

/// Constrained triangles
vector<Triangle*> triangles;
/// Triangle map
list<Triangle*> map;
/// Polylines
vector<Point> polyline;
vector<vector<Point>> holes;
vector<Point> steiner;

/// Draw the entire triangle map?
bool draw_map = false;
/// Create a random distribution of points?
bool random_distribution = false;

GLFWwindow* window = nullptr;

int main(int argc, char* argv[])
{
  string filename;
  size_t num_points = 0u;
  double max, min;
  double zoom;

  if (argc != 2 && argc != 5) {
    cout << "-== USAGE ==-" << endl;
    cout << "Load Data File: p2t <filename> <center_x> <center_y> <zoom>" << endl;
    cout << "  Example: build/testbed/p2t testbed/data/dude.dat 350 500 3" << endl;
    cout << "Load Data File with Auto-Zoom: p2t <filename>" << endl;
    cout << "  Example: build/testbed/p2t testbed/data/nazca_monkey.dat" << endl;
    cout << "Generate Random Polygon: p2t random <num_points> <box_radius> <zoom>" << endl;
    cout << "  Example: build/testbed/p2t random 100 1 500" << endl;
    return 1;
  }

  // If true, adjust the zoom settings to fit the input geometry to the window
  const bool autozoom = (argc == 2);

  if (!autozoom && string(argv[1]) == "random") {
    num_points = atoi(argv[2]);
    random_distribution = true;
    char* pEnd;
    max = strtod(argv[3], &pEnd);
    min = -max;
    cx = cy = 0.0;
    zoom = atof(argv[4]);
  } else {
    filename = string(argv[1]);
    if (!autozoom) {
      cx = atof(argv[2]);
      cy = atof(argv[3]);
      zoom = atof(argv[4]);
    }
  }

  if (random_distribution) {
    GenerateRandomPointDistribution(num_points, min, max, polyline, holes, steiner);
  } else {
    // Load pointset from file
    if (!ParseFile(filename, polyline, holes, steiner)) {
      return 2;
    }
  }

  if (autozoom) {
    assert(0.0 <= autozoom_border && autozoom_border < 1.0);
    const auto bbox = BoundingBox(polyline);
    Point center = bbox.first + bbox.second;
    center *= 0.5;
    cx = center.x;
    cy = center.y;
    Point sides = bbox.second - bbox.first;
    zoom = 2.0 * (1.0 - autozoom_border) * std::min((double)default_window_width / sides.x, (double)default_window_height / sides.y);
    std::cout << "center_x = " << cx << std::endl;
    std::cout << "center_y = " << cy << std::endl;
    std::cout << "zoom = " << zoom << std::endl;
  }

  Init(default_window_width, default_window_height);

  /*
   * Perform triangulation!
   */

  double init_time = glfwGetTime();

  /*
   * STEP 1: Create CDT and add primary polyline
   * NOTE: polyline must be a simple polygon. The polyline's points
   * constitute constrained edges. No repeat points!!!
   */
  CDT* cdt = new CDT(polyline);

  /*
   * STEP 2: Add holes or Steiner points
   */
  for (auto& hole : holes) {
    assert(!hole.empty());
    cdt->AddHole(hole);
  }
  for (auto& s : steiner) {
    cdt->AddPoint(&s);
  }

  /*
   * STEP 3: Triangulate!
   */
  cdt->Triangulate();

  double dt = glfwGetTime() - init_time;

  triangles = cdt->GetTriangles();
  map = cdt->GetMap();
  const size_t points_in_holes =
      std::accumulate(holes.cbegin(), holes.cend(), size_t(0),
                      [](size_t cumul, const vector<Point>& hole) { return cumul + hole.size(); });

  cout << "Number of primary constrained edges = " << polyline.size() << endl;
  cout << "Number of holes = " << holes.size() << endl;
  cout << "Number of constrained edges in holes = " << points_in_holes << endl;
  cout << "Number of Steiner points = " << steiner.size() << endl;
  cout << "Total number of points = " << (polyline.size() + points_in_holes + steiner.size())
       << endl;
  cout << "Number of triangles = " << triangles.size() << endl;
  cout << "Is Delaunay = " << (IsDelaunay(triangles) ? "true" : "false") << endl;
  cout << "Elapsed time (ms) = " << dt * 1000.0 << endl;

  MainLoop(zoom);

  // Cleanup
  delete cdt;
  polyline.clear();
  for (vector<Point>& hole : holes) {
    hole.clear();
  }
  steiner.clear();

  ShutDown(0);
  return 0;
}

bool ParseFile(string filename, vector<Point>& out_polyline, vector<vector<Point>>& out_holes,
               vector<Point>& out_steiner)
{
  enum ParserState {
    Polyline,
    Hole,
    Steiner,
  };
  ParserState state = Polyline;
  vector<Point>* hole = nullptr;
  try {
    string line;
    ifstream myfile(filename);
    if (myfile.is_open()) {
      while (!myfile.eof()) {
        getline(myfile, line);
        if (line.empty()) {
          break;
        }
        istringstream iss(line);
        vector<string> tokens;
        copy(istream_iterator<string>(iss), istream_iterator<string>(), back_inserter(tokens));
        if (tokens.empty()) {
          break;
        } else if (tokens.size() == 1u) {
          const auto token = tokens[0];
          if (token == "HOLE") {
            state = Hole;
            out_holes.emplace_back();
            hole = &out_holes.back();
          } else if (token == "STEINER") {
            state = Steiner;
          } else {
            throw runtime_error("Invalid token [" + token + "]");
          }
        } else {
          double x = StringToDouble(tokens[0]);
          double y = StringToDouble(tokens[1]);
          switch (state) {
            case Polyline:
              out_polyline.push_back(Point(x, y));
              break;
            case Hole:
              assert(hole != nullptr);
              hole->push_back(Point(x, y));
              break;
            case Steiner:
              out_steiner.push_back(Point(x, y));
              break;
            default:
              assert(0);
          }
        }
      }
    } else {
      throw runtime_error("File not opened");
    }
  } catch (exception& e) {
    cerr << "Error parsing file: " << e.what() << endl;
    return false;
  }
  return true;
}

std::pair<Point, Point> BoundingBox(gsl::span<const Point> polyline)
{
  assert(polyline.size() > 0);
  using Scalar = decltype(p2t::Point::x);
  Point min(std::numeric_limits<Scalar>::max(), std::numeric_limits<Scalar>::max());
  Point max(std::numeric_limits<Scalar>::min(), std::numeric_limits<Scalar>::min());
  for (const Point& point : polyline) {
    min.x = std::min(min.x, point.x);
    min.y = std::min(min.y, point.y);
    max.x = std::max(max.x, point.x);
    max.y = std::max(max.y, point.y);
  }
  return std::make_pair(min, max);
}

void GenerateRandomPointDistribution(size_t num_points, double min, double max,
                                     vector<Point>& out_polyline,
                                     vector<vector<Point>>& out_holes, vector<Point>& out_steiner)
{
  out_polyline.push_back(Point(min, min));
  out_polyline.push_back(Point(min, max));
  out_polyline.push_back(Point(max, max));
  out_polyline.push_back(Point(max, min));

  max -= (1e-4);
  min += (1e-4);
  for (int i = 0; i < num_points; i++) {
    double x = Random(Fun, min, max);
    double y = Random(Fun, min, max);
    out_steiner.push_back(Point(x, y));
  }
}

void Init(int window_width, int window_height)
{
  if (glfwInit() != GL_TRUE)
    ShutDown(1);
  // width x height, 16 bit color, no depth, alpha or stencil buffers, windowed
  window = glfwCreateWindow(window_width, window_height, "Poly2Tri - C++", NULL, NULL);
  if (!window)
    ShutDown(1);

  glfwMakeContextCurrent(window);
  glfwSwapInterval(1);

  glEnable(GL_BLEND);
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
  glClearColor(0.0, 0.0, 0.0, 0.0);
  glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
}

void ShutDown(int return_code)
{
  glfwTerminate();
  exit(return_code);
}

void MainLoop(const double zoom)
{
  // the time of the previous frame
  double old_time = glfwGetTime();
  // this just loops as long as the program runs
  bool running = true;

  while (running) {
    glfwPollEvents();

    // calculate time elapsed, and the amount by which stuff rotates
    double current_time = glfwGetTime(),
           delta_rotate = (current_time - old_time) * rotations_per_tick * 360.0;
    old_time = current_time;

    // escape to quit, arrow keys to rotate view
    // Check if ESC key was pressed or window was closed
    running = !glfwGetKey(window, GLFW_KEY_ESCAPE) && !glfwWindowShouldClose(window);

    if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)
      rotate_y += delta_rotate;
    if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS)
      rotate_y -= delta_rotate;
    // z axis always rotates
    rotate_z += delta_rotate;

    // Draw the scene
    if (draw_map) {
      DrawMap(zoom);
    } else {
      Draw(zoom);
    }

    // swap back and front buffers
    glfwSwapBuffers(window);
  }
}

void ResetZoom(double zoom, double cx, double cy, double width, double height)
{
  double left = -width / zoom;
  double right = width / zoom;
  double bottom = -height / zoom;
  double top = height / zoom;

  // Reset viewport
  glLoadIdentity();
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();

  // Reset ortho view
  glOrtho(left, right, bottom, top, 1.0, -1.0);
  glTranslated(-cx, -cy, 0.0);
  glMatrixMode(GL_MODELVIEW);
  glDisable(GL_DEPTH_TEST);
  glLoadIdentity();

  // Clear the screen
  glClear(GL_COLOR_BUFFER_BIT);
}

void Draw(const double zoom)
{
  // reset zoom
  Point center = Point(cx, cy);

  ResetZoom(zoom, center.x, center.y, (double)default_window_width, (double)default_window_height);

  for (int i = 0; i < triangles.size(); i++) {
    Triangle& t = *triangles[i];
    Point& a = *t.GetPoint(0);
    Point& b = *t.GetPoint(1);
    Point& c = *t.GetPoint(2);

    // Red
    glColor3f(1, 0, 0);

    glBegin(GL_LINE_LOOP);
    glVertex2d(a.x, a.y);
    glVertex2d(b.x, b.y);
    glVertex2d(c.x, c.y);
    glEnd();
  }

  // green
  glColor3f(0, 1, 0);

  vector<vector<Point>*> polylines;
  polylines.push_back(&polyline);
  for (vector<Point>& hole : holes) {
      polylines.push_back(&hole);
  }
  for(int i = 0; i < polylines.size(); i++) {
    const vector<Point>& poly = *polylines[i];
    glBegin(GL_LINE_LOOP);
      for(int j = 0; j < poly.size(); j++) {
        glVertex2d(poly[j].x, poly[j].y);
      }
    glEnd();
  }
}

void DrawMap(const double zoom)
{
  // reset zoom
  Point center = Point(cx, cy);

  ResetZoom(zoom, center.x, center.y, (double)default_window_width, (double)default_window_height);

  list<Triangle*>::iterator it;
  for (it = map.begin(); it != map.end(); it++) {
    Triangle& t = **it;
    Point& a = *t.GetPoint(0);
    Point& b = *t.GetPoint(1);
    Point& c = *t.GetPoint(2);

    ConstrainedColor(t.constrained_edge[2]);
    glBegin(GL_LINES);
    glVertex2d(a.x, a.y);
    glVertex2d(b.x, b.y);
    glEnd( );

    ConstrainedColor(t.constrained_edge[0]);
    glBegin(GL_LINES);
    glVertex2d(b.x, b.y);
    glVertex2d(c.x, c.y);
    glEnd( );

    ConstrainedColor(t.constrained_edge[1]);
    glBegin(GL_LINES);
    glVertex2d(c.x, c.y);
    glVertex2d(a.x, a.y);
    glEnd( );
  }
}

void ConstrainedColor(bool constrain)
{
  if (constrain) {
    // Green
    glColor3f(0, 1, 0);
  } else {
    // Red
    glColor3f(1, 0, 0);
  }
}

double StringToDouble(const std::string& s)
{
  std::istringstream i(s);
  double x;
  if (!(i >> x))
    return 0;
  return x;
}

double Fun(double x)
{
  return 2.5 + sin(10 * x) / x;
}

double Random(double (*fun)(double), double xmin = 0, double xmax = 1)
{
  static double (*Fun)(double) = NULL, YMin, YMax;
  static bool First = true;

  // Initialises random generator for first call
  if (First)
  {
    First = false;
    srand((unsigned) time(NULL));
  }

  // Evaluates maximum of function
  if (fun != Fun)
  {
    Fun = fun;
    YMin = 0, YMax = Fun(xmin);
    for (int iX = 1; iX < RAND_MAX; iX++)
    {
      double X = xmin + (xmax - xmin) * iX / RAND_MAX;
      double Y = Fun(X);
      YMax = Y > YMax ? Y : YMax;
    }
  }

  // Gets random values for X & Y
  double X = xmin + (xmax - xmin) * rand() / RAND_MAX;
  double Y = YMin + (YMax - YMin) * rand() / RAND_MAX;

  // Returns if valid and try again if not valid
  return Y < fun(X) ? X : Random(Fun, xmin, xmax);
}

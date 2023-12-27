#include <pybind11/pybind11.h>
#include <pybind11/stl.h>
#include <pybind11/eigen.h>

#include <poly2tri/poly2tri.h>
#include <Eigen/Dense>

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

#define STRINGIFY(x) #x
#define MACRO_STRINGIFY(x) STRINGIFY(x)

namespace py = pybind11;
using namespace pybind11::literals;
using rvp = py::return_value_policy;

#if __cplusplus > 201703L
// For C++20, use std::span directly.
template <typename T> using span = std::span<T>;
#else
// For C++17, use a minimal polyfill for std::span sufficient for pixelmatch.
template <typename T> class span {
public:
  constexpr span() = default;
  ~span() = default;

  constexpr span(T* data, size_t size) : data_(data), size_(size)
  {
  }

  template <typename Container,
            std::enable_if_t<
                std::is_pointer<decltype(std::declval<Container&>().data())>::value &&
                    std::is_convertible<
                        std::remove_pointer_t<decltype(std::declval<Container&>().data())> (*)[],
                        T (*)[]>::value,
                int> = 0>
  constexpr span(Container& container) : span(container.data(), container.size())
  {
  }

  span(const span&) = default;
  span& operator=(const span&) = default;

  const T* data() const
  {
    return data_;
  }
  size_t size() const
  {
    return size_;
  }
  bool empty() const
  {
    return size_ == 0;
  }

  T operator[](size_t index) const
  {
    return data_[index];
  }
  T& operator[](size_t index)
  {
    return data_[index];
  }

private:
  T* data_ = nullptr;
  size_t size_ = 0;
};
#endif

inline void pixelmatch_fn(const py::buffer& pointcloud)
{
  auto buf = pointcloud.request();
  span<const double> pcd(reinterpret_cast<const double*>(buf.ptr), buf.size);
}

template <class C> void FreeClear(C& cntr)
{
  for (typename C::iterator it = cntr.begin(); it != cntr.end(); ++it) {
    delete *it;
  }
  cntr.clear();
}

using RowVectors = Eigen::Matrix<double, Eigen::Dynamic, 2, Eigen::RowMajor>;

inline std::vector<p2t::Point*> from_eigen(const RowVectors& xys)
{
  std::vector<p2t::Point*> ret;
  ret.reserve(xys.size());
  for (int i = 0, N = xys.size(); i < N; ++i) {
    ret.push_back(new p2t::Point(xys(i, 0), xys(i, 1)));
  }
  return ret;
}

PYBIND11_MODULE(_core, m)
{
  m.def(
      "cdt",
      [](const RowVectors& polyline_,           //
         const std::vector<RowVectors>& holes_, //
         const RowVectors& steiner_) {
        using namespace std;
        using namespace p2t;

        vector<Point*> polyline = from_eigen(polyline_);
        vector<vector<Point*>> holes;
        holes.reserve(holes_.size());
        for (auto& hole : holes_) {
          holes.push_back(from_eigen(hole));
        }
        vector<Point*> steiner = from_eigen(steiner_);

        /*
         * STEP 1: Create CDT and add primary polyline
         * NOTE: polyline must be a simple polygon. The polyline's points
         * constitute constrained edges. No repeat points!!!
         */
        auto cdt = new CDT(polyline);

        /*
         * STEP 2: Add holes or Steiner points
         */
        for (const auto& hole : holes) {
          assert(!hole.empty());
          cdt->AddHole(hole);
        }
        for (const auto& s : steiner) {
          cdt->AddPoint(s);
        }

        /*
         * STEP 3: Triangulate!
         */
        cdt->Triangulate();

        auto triangles = cdt->GetTriangles();
        auto map = cdt->GetMap();
        const size_t points_in_holes = std::accumulate(
            holes.cbegin(), holes.cend(), size_t(0),
            [](size_t cumul, const vector<Point*>& hole) { return cumul + hole.size(); });

        cout << "Number of primary constrained edges = " << polyline.size() << endl;
        cout << "Number of holes = " << holes.size() << endl;
        cout << "Number of constrained edges in holes = " << points_in_holes << endl;
        cout << "Number of Steiner points = " << steiner.size() << endl;
        cout << "Total number of points = " << (polyline.size() + points_in_holes + steiner.size())
             << endl;
        cout << "Number of triangles = " << triangles.size() << endl;
        cout << "Is Delaunay = " << (IsDelaunay(triangles) ? "true" : "false") << endl;

        // Cleanup
        delete cdt;
        FreeClear(polyline);
        for (vector<Point*>& hole : holes) {
          FreeClear(hole);
        }
        FreeClear(steiner);
      },
      py::kw_only(), "polyline"_a, "holes"_a, "steiner"_a);

#ifdef VERSION_INFO
  m.attr("__version__") = MACRO_STRINGIFY(VERSION_INFO);
#else
  m.attr("__version__") = "dev";
#endif
}

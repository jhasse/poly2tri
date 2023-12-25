#include <pybind11/pybind11.h>
#include <pybind11/stl.h>

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

PYBIND11_MODULE(_core, m)
{
#ifdef VERSION_INFO
  m.attr("__version__") = MACRO_STRINGIFY(VERSION_INFO);
#else
  m.attr("__version__") = "dev";
#endif
}

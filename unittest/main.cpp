#define BOOST_TEST_DYN_LINK
#define BOOST_TEST_MODULE Poly2triTest
#include <boost/test/unit_test.hpp>
#include <poly2tri/poly2tri.h>
#include <fstream>
#include <iostream>

BOOST_AUTO_TEST_CASE(BasicTest)
{
  std::vector<p2t::Point*> polyline{
    new p2t::Point(0, 0),
    new p2t::Point(1, 0),
    new p2t::Point(1, 1),
  };
  p2t::CDT cdt{ polyline };
  cdt.Triangulate();
  const auto result = cdt.GetTriangles();
  BOOST_REQUIRE_EQUAL(result.size(), 1);
  BOOST_CHECK_EQUAL(*result[0]->GetPoint(0), *polyline[0]);
  BOOST_CHECK_EQUAL(*result[0]->GetPoint(1), *polyline[1]);
  BOOST_CHECK_EQUAL(*result[0]->GetPoint(2), *polyline[2]);
  BOOST_CHECK(p2t::IsDelaunay(result));
  for (const auto p : polyline) {
    delete p;
  }
}

BOOST_AUTO_TEST_CASE(QuadTest)
{
  std::vector<p2t::Point*> polyline{ new p2t::Point(0, 0), new p2t::Point(0, 1),
                                     new p2t::Point(1, 1), new p2t::Point(1, 0) };
  p2t::CDT cdt{ polyline };
  cdt.Triangulate();
  const auto result = cdt.GetTriangles();
  BOOST_REQUIRE_EQUAL(result.size(), 2);
  BOOST_CHECK(p2t::IsDelaunay(result));
  for (const auto p : polyline) {
    delete p;
  }
}

BOOST_AUTO_TEST_CASE(TestbedFilesTest)
{
  for (const auto& filename : { "custom.dat", "diamond.dat", "star.dat", "test.dat" }) {
    std::vector<p2t::Point*> polyline;
    // Load pointset from file
    // Parse and tokenize data file
    std::string line;
    const std::string src(__FILE__); // ../unittest/main.cpp
    auto folder = src.substr(0, src.find_last_of('/')) + "/../testbed/data/";
    std::ifstream myfile(folder + filename);
    BOOST_REQUIRE(myfile.is_open());
    while (!myfile.eof()) {
      getline(myfile, line);
      if (line.empty()) {
        break;
      }
      std::istringstream iss(line);
      std::vector<std::string> tokens;
      copy(std::istream_iterator<std::string>(iss), std::istream_iterator<std::string>(),
           std::back_inserter<std::vector<std::string>>(tokens));
      double x = std::stod(tokens[0]);
      double y = std::stod(tokens[1]);
      polyline.push_back(new p2t::Point(x, y));
    }
    p2t::CDT cdt{ polyline };
    cdt.Triangulate();
    const auto result = cdt.GetTriangles();
    BOOST_REQUIRE(result.size() * 3 > polyline.size());
    BOOST_CHECK_MESSAGE(p2t::IsDelaunay(result), filename + std::to_string(polyline.size()));
    for (const auto p : polyline) {
      delete p;
    }
  }
}

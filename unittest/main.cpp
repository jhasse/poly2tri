#define BOOST_TEST_DYN_LINK
#define BOOST_TEST_MODULE Poly2triTest
#include <boost/test/unit_test.hpp>
#include <poly2tri/poly2tri.h>
#include <iostream>

BOOST_AUTO_TEST_CASE(BasicTest) {
	std::vector<p2t::Point*> polyline{
		new p2t::Point(0, 0), new p2t::Point(1, 0), new p2t::Point(1, 1),
	};
	p2t::CDT cdt{polyline};
	cdt.Triangulate();
	const auto result = cdt.GetTriangles();
	assert(result.size() == 1);
	BOOST_CHECK_EQUAL(*result[0]->GetPoint(0), *polyline[0]);
	BOOST_CHECK_EQUAL(*result[0]->GetPoint(1), *polyline[1]);
	BOOST_CHECK_EQUAL(*result[0]->GetPoint(2), *polyline[2]);
	result[0]->DebugPrint();
}

project('poly2tri', ['cpp'])

include = include_directories('.')
lib = static_library('poly2tri', sources : [
	'poly2tri/common/shapes.cc',
	'poly2tri/sweep/advancing_front.cc',
	'poly2tri/sweep/cdt.cc',
	'poly2tri/sweep/sweep.cc',
	'poly2tri/sweep/sweep_context.cc',
])

thread_dep = dependency('threads')
boost_test_dep = dependency('boost', modules : [ 'filesystem', 'unit_test_framework' ], required : false)
if boost_test_dep.found()
	test('Unit Test', executable('unittest', [
		'unittest/main.cpp',
		'unittest/TriangleTest.cpp',
	], dependencies : [boost_test_dep, thread_dep], link_with : lib))
endif

poly2tri_dep = declare_dependency(include_directories : include, link_with : lib)

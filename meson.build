project('poly2tri', ['cpp'])

include = include_directories('.')
lib = static_library('poly2tri', sources : [
	'poly2tri/common/shapes.cc',
	'poly2tri/sweep/advancing_front.cc',
	'poly2tri/sweep/cdt.cc',
	'poly2tri/sweep/sweep.cc',
	'poly2tri/sweep/sweep_context.cc',
])

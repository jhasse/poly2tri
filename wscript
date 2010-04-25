VERSION='0.0.1'
import sys
APPNAME='p2t'
srcdir = '.'
blddir = 'build'

p2t_source_files = ['poly2tri/common/shapes.cc',
                    'poly2tri/sweep/cdt.cc',
                    'poly2tri/sweep/advancing_front.cc',
                    'poly2tri/sweep/sweep_context.cc',
                    'poly2tri/sweep/sweep.cc']

testbed_source_files = ['testbed/main.cc']

#Platform specific libs
if sys.platform == 'win32':
    # MS Windows
    sys_libs = ['glfw', 'opengl32']
elif sys.platform == 'darwin':
    # Apple OSX
    sys_libs = ['glfw', 'OpenGL']
else:
    # GNU/Linux, BSD, etc
    sys_libs = ['glfw', 'GL']

def init():
    print('  init called')

def set_options(opt):
	print('  set_options')
	opt.tool_options('g++')

def configure(conf):
	print('  calling the configuration')
	conf.check_tool('g++')
    #conf.env.CXXFLAGS = ['-O0', '-pg', '-g']
	conf.env.CXXFLAGS = ['-O0', '-g']
	#conf.env.CXXFLAGS = ['-O3', '-ffast-math']

def build(bld):

  print('  building')

  '''
  # A static library
  # The extension (.a) is added automatically
  bld.new_task_gen(
    features = 'cxx cshlib',
    source = p2t_source_files,
    name = 'poly2tri',
    target = 'poly2tri')

  # 1. A simple program
  bld.new_task_gen(
    features = 'cxx cprogram',
    source = testbed_source_files,
    target = 'p2t',
    uselib_local = 'poly2tri',
    libs = sys_libs)
  '''

  bld.new_task_gen(
    features = 'cxx cprogram',
    source = testbed_source_files + p2t_source_files,
    target = 'p2t',
    libs = sys_libs)

def shutdown():
    print('  shutdown called')

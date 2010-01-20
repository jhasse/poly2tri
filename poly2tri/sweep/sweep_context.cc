#include "sweep_context.h"

#include <algorithm>
#include <GL/glfw.h>
#include "advancing_front.h"

SweepContext::SweepContext(Point** polyline, const int& point_count) {

  basin = Basin();
  edge_event = EdgeEvent();
	
  points_ = polyline;
  point_count_ = point_count;
  
  InitEdges(points_, point_count_);
  InitTriangulation();
  
}

std::vector<Triangle*> SweepContext::GetTriangles() {
  return triangles_;
}

std::list<Triangle*> SweepContext::GetMap() {
  return map_;
}

void SweepContext::InitTriangulation() {

  double xmax(points_[0]->x), xmin(points_[0]->x);
  double ymax(points_[0]->y), ymin(points_[0]->y);
  
  // Calculate bounds. 
  for(int i = 0; i < point_count_; i++) {
    Point p = *points_[i];
    if(p.x > xmax)
        xmax = p.x;
    if(p.x < xmin)
        xmin = p.x;
    if(p.y > ymax)
        ymax = p.y;
    if(p.y < ymin)
        ymin = p.y;
  }

  double dx = kAlpha * ( xmax - xmin );
  double dy = kAlpha * ( ymax - ymin );
  head_ = new Point(xmax + dx, ymin - dy);
  tail_ = new Point(xmin - dx, ymin - dy);
    
  // Sort points along y-axis
  double init_time = glfwGetTime();
  std::sort(points_, points_ + point_count_, cmp);
  double dt = glfwGetTime() - init_time;
  printf("Sort time (secs) = %f\n", dt);
  
  /*
  printf("*************************\n");
  for(int i = 0; i < point_count_; i++) {
    printf("%f,%f ", points_[i]->x, points_[i]->y);
    printf("%p\n", points_[i]);
  }
  
  printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
  for(int i = 0; i < edge_list.size(); i++) {
    edge_list[i]->p->DebugPrint(); edge_list[i]->q->DebugPrint();
    printf("%p, %p\n", edge_list[i]->p, edge_list[i]->q);
  }
  */
  
}

void SweepContext::InitEdges(Point** polyline, const int& point_count) {

  for(int i = 0; i < point_count; i++) {
    int j = i < point_count - 1 ? i + 1 : 0;
    edge_list.push_back(new Edge(*polyline[i], *polyline[j]));
  }
  
  /*
  for(int i = 0; i < edge_list.size(); i++) {
    edge_list[i]->p->DebugPrint(); edge_list[i]->q->DebugPrint();
    printf("%p, %p\n", edge_list[i]->p, edge_list[i]->q);
  }
  */
  
}
    
Point* SweepContext::GetPoint(const int& index) { 
	return points_[index];
}

void SweepContext::AddToMap(Triangle* triangle ) {
  map_.push_back(triangle);
}

Node& SweepContext::LocateNode(Point& point) {
	// TODO implement search tree
	return *front_->Locate(point.x);
}

void SweepContext::CreateAdvancingFront() {
	
	// Initial triangle
	Triangle* triangle = new Triangle(*points_[0], *tail_, *head_);

	map_.push_back(triangle);
	
	front_ = new AdvancingFront;
        
	front_->set_head(new Node(*triangle->GetPoint(1)));
	front_->head()->triangle = triangle;
	Node* middle = new Node(*triangle->GetPoint(0));
	middle->triangle = triangle;
	front_->set_tail(new Node(*triangle->GetPoint(2)));
	front_->set_search(middle);
	
	// TODO: More intuitive if head is middles next and not previous?
	//       so swap head and tail
	front_->head()->next = middle;
	middle->next = front_->tail();
	middle->prev = front_->head();
	front_->tail()->prev = middle;
	
}
		
void SweepContext::RemoveNode(Node* node) {
	delete node;
}
		
void SweepContext::MapTriangleToNodes(Triangle& t) {
  for(int i=0; i<3; i++) {
    if(t.GetNeighbor(i) == NULL) {
      Node* n = front_->LocatePoint(t.PointCW(*t.GetPoint(i)));
      if(n) 
        n->triangle = &t;
    }            
  }        
}

void SweepContext::RemoveFromMap(Triangle* triangle) {
  map_.remove(triangle);
}

void SweepContext::MeshClean(Triangle& triangle ) {

  if(&triangle != NULL && !triangle.IsInterior()) {
    triangle.IsInterior(true);
    triangles_.push_back(&triangle);
    for(int i = 0; i < 3; i++) {
      if(!triangle.constrained_edge[i])
        MeshClean(*triangle.GetNeighbor(i));     
    }
  }
}

SweepContext::~SweepContext() {
  delete head_;
  delete tail_;
  delete front_;
}

#include "sweep_context.h"

#include <algorithm>
#include <GL/glfw.h>
#include "advancing_front.h"

SweepContext::SweepContext(Point** polyline, const int& point_count) {

  basin = Basin();
  edge_event = EdgeEvent();
	
  for(int i = 0; i < point_count; i++) {
    points_.push_back(**&polyline[i]);
  }
  
  InitEdges(polyline, point_count);
  InitTriangulation();
  
}

std::list<Triangle*> SweepContext::GetTriangles() {
  return tri_list_;
}

void SweepContext::InitTriangulation() {

  double xmax(points_[0].x), xmin(points_[0].x);
  double ymax(points_[0].y), ymin(points_[0].y);
  
  // Calculate bounds. 
  for(int i = 0; i < points_.size(); i++) {
    Point p = points_[i];
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
  std::sort(points_.begin(), points_.end());
  double dt = glfwGetTime() - init_time;
  printf("Sort time (secs) = %f\n", dt);

}

void SweepContext::InitEdges(Point** polyline, const int& point_count) {

  for(int i = 0; i < point_count; i++) {
    int j = i < points_.size() - 1 ? i + 1 : 0;
    edge_list.push_back(new Edge(**&polyline[i], **&polyline[j]));
  }
}
    
Point* SweepContext::GetPoint(const int& index) { 
	return &points_[index];
}

void SweepContext::AddToMap(Triangle* triangle ) {
  tri_list_.push_back(triangle);
}

Node& SweepContext::LocateNode(Point& point) {
	// TODO implement search tree
	return *front_->Locate(point.x);
}

void SweepContext::CreateAdvancingFront() {
	
	// Initial triangle
	Triangle* triangle = new Triangle(points_[0], *tail_, *head_);

	tri_list_.push_back(triangle);
	
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
  tri_list_.remove(triangle);
}

/*
void SweepContext::MeshClean(Triangle& triangle) {
  pointset_.ClearTriangulation();
  MeshCleanReq(triangle);
}

AFront SweepContext::front_() {
  return front_;
}
    
void SweepContext::Clear() {
  super.clear();
  tri_list_.Clear();
}


Node* SweepContext::LocateNode(Point& point) {
  // TODO implement tree
  return front_.Locate(point.x);
}

/*

void SweepContext::MeshCleanReq(Triangle& triangle ) {
  if(triangle != NULL && !triangle.isInterior()) {
    triangle.IsInterior(true);
    pointset_.AddTriangle(triangle);
    for(int i = 0; i < 3; i++) {
      if(!triangle.cEdge[i])
        MeshCleanReq(triangle.neighbors[i]);     
		}
	}
}
*/

SweepContext::~SweepContext() {
  //delete [] points_;
  delete head_;
  delete tail_;
  delete front_;
}

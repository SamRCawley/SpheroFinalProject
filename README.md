Final Project for Operating Systems


Roll(float distance, float speed);
Rolls the robot for the length of the distance given in meters using the current heading at the speed between 0 and 1, where 1 is the maximum possible speed achievable by Sphero.

Turn(int angle);
Turns the robot in place by the number of degrees specified in angle, from 0 to 360.

Arc(float radius, int angle, float speed);
Rolls the robot at an arc specified by radius and angle starting at the current heading at the given speed.

Color(int R, int G, int B);
Set the color of the robot to a given RGB value.
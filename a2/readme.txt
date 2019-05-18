.7 mode in this program:
    1.world view of the frustums and objects.
	2.The view from the eye that lies on the world z axis.
    3.The blurred depth of field view from the eye that lies on the world z axis.
    4.The view from the left eye. 
    5.The view from the right eye. 
    6.An anaglyph showing both left and right eyes in the same image.
    7.The combination of a depth of field blur for each eye show as an anaglyph.
	
.when drawEyeFrustums.getValue() == true, we have two eyes(left and right), then we have two focal planes.
.to make the blur of depth, the frustum must keep the focal plane rectangle fixed. 

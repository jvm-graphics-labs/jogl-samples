uniform mat4 MVP;

attribute highp vec4 Position;

void main()
{	
	gl_Position = MVP * Position;
}


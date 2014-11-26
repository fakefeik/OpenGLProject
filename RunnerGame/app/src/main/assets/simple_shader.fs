precision mediump float;

varying vec4 v_Color;
varying vec2 v_TexCoordinate;

uniform sampler2D u_Texture;

void main() {
    gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
}

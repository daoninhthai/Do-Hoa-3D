var VSHADER_SOURCE =
    'attribute vec4 a_Position;\n' +
    'attribute vec4 a_Color;\n' +
    'attribute vec4 a_Normal;\n' +
    'uniform mat4 u_MvpMatrix;\n' +
    'uniform mat4 u_ModelMatrix;\n' +
    'uniform mat4 u_NormalMatrix;\n' +
    'uniform bool u_Clicked;\n' +
    'varying vec4 v_Color;\n' +
    'varying vec3 v_Normal;\n' +
    'varying vec3 v_Position;\n' +
    'void main() {\n' +
    '  gl_Position = u_MvpMatrix * a_Position;\n' +
    '  v_Position = vec3(u_ModelMatrix * a_Position);\n' +
    '  v_Normal = normalize(vec3(u_NormalMatrix * a_Normal));\n' +
    '  if (u_Clicked) {\n' +
    '    v_Color = vec4(0.0, 0.0, 0.0, 1.0);\n' +
    '  } else {\n' +
    '    v_Color = vec4(a_Color.rgb, 1.0);\n' +
    '  }\n' +
    '}\n';
var FSHADER_SOURCE =
    '#ifdef GL_ES\n' +
    'precision mediump float;\n' +
    '#endif\n' +
    'uniform vec3 u_LightColor;\n' +
    'uniform vec3 u_LightPosition;\n' +
    'uniform vec3 u_AmbientLight;\n' +
    'varying vec3 v_Normal;\n' +
    'varying vec3 v_Position;\n' +
    'varying vec4 v_Color;\n' +
    'void main() {\n' +
    '  vec3 normal = normalize(v_Normal);\n' +
    '  vec3 lightDirection = normalize(u_LightPosition - v_Position);\n' +
    '  float nDotL = max(dot(lightDirection, normal), 0.0);\n' +
    '  vec3 diffuse = u_LightColor * v_Color.rgb * nDotL;\n' +
    '  vec3 ambient = u_AmbientLight * v_Color.rgb;\n' +
    '  gl_FragColor = vec4(diffuse + ambient, v_Color.a);\n' +
    '}\n';

function main() {
    var canvas = document.getElementById('webgl');
    var gl = getWebGLContext(canvas);
    if (!gl) {
        console.log('Failed to get the rendering context for WebGL');
        return;
    }
    if (!initShaders(gl, VSHADER_SOURCE, FSHADER_SOURCE)) {
        console.log('Failed to intialize shaders.');
        return;
    }
    var n = initVertexBuffers(gl);
    if (n < 0) {
        console.log('Failed to set the vertex information');
        return;
    }
    gl.clearColor(0.0, 0.0, 0.0, 0.0);
    gl.enable(gl.DEPTH_TEST);
    var u_ModelMatrix = gl.getUniformLocation(gl.program, 'u_ModelMatrix');
    var u_MvpMatrix = gl.getUniformLocation(gl.program, 'u_MvpMatrix');
    var u_NormalMatrix = gl.getUniformLocation(gl.program, 'u_NormalMatrix');
    var u_LightColor = gl.getUniformLocation(gl.program, 'u_LightColor');
    var u_LightPosition = gl.getUniformLocation(gl.program, 'u_LightPosition');
    var u_AmbientLight = gl.getUniformLocation(gl.program, 'u_AmbientLight');
    var u_Clicked = gl.getUniformLocation(gl.program, 'u_Clicked');
    if (!u_MvpMatrix || !u_NormalMatrix || !u_LightColor || !u_LightPosition　 || !u_AmbientLight) {
        console.log('Failed to get the storage location');
        return;
    }
    x = 6.0;
    y = 3.0;
    z = 0.0;
    var viewProjMatrix = new Matrix4();
    document.getElementById('change_view').addEventListener('click', function() {
        x = document.getElementById('page_x').value;
        y = document.getElementById('page_y').value;
        z = document.getElementById('page_z').value;
        viewProjMatrix.setPerspective(30.0, canvas.width / canvas.height, 1.0, 100.0);
        viewProjMatrix.lookAt(x, y, z, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
    })
    lx = 2.3;
    ly = 4.0;
    lz = 3.5;
    document.getElementById('change_light').addEventListener('click', function() {
        lx = document.getElementById('l_x').value;
        ly = document.getElementById('l_y').value;
        lz = document.getElementById('l_z').value;
        gl.uniform3f(u_LightPosition, lx, ly, lz);
    })
    viewProjMatrix.setPerspective(30.0, canvas.width / canvas.height, 1.0, 100.0);
    viewProjMatrix.lookAt(x, y, z, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
    gl.uniform3f(u_LightColor, 1.0, 1.0, 1.0);
    gl.uniform3f(u_AmbientLight, 1, 1, 1);
    gl.uniform1i(u_Clicked, 0);
    var currentAngle = 0.0;
    canvas.onmousedown = function(ev) {
        var x = ev.clientX,
            y = ev.clientY;
        var rect = ev.target.getBoundingClientRect();
        if (rect.left <= x && x < rect.right && rect.top <= y && y < rect.bottom) {
            var x_in_canvas = x - rect.left,
                y_in_canvas = rect.bottom - y;
            check(gl, n, x_in_canvas, y_in_canvas, currentAngle, u_Clicked, viewProjMatrix, u_MvpMatrix);
        }
    }
    var modelMatrix = new Matrix4();
    var mviewProjMatrix = new Matrix4();
    var normalMatrix = new Matrix4();
    ttx = 1;
    tty = 0;
    ttz = 0;
    document.getElementById('change_hc').addEventListener('click', function() {
        ttx = document.getElementById('tt_x').value;
        tty = document.getElementById('tt_y').value;
        ttz = document.getElementById('tt_z').value;
    })
    ttx1 = 0;
    tty1 = 0;
    ttz1 = 0;
    document.getElementById('change_hc1').addEventListener('click', function() {
        ttx1 = document.getElementById('tt1_x').value;
        tty1 = document.getElementById('tt1_y').value;
        ttz1 = document.getElementById('tt1_z').value;
    })
    ttx2 = 0;
    tty2 = 0;
    ttz2 = 0;
    document.getElementById('change_hc2').addEventListener('click', function() {
        ttx2 = document.getElementById('tt2_x').value;
        tty2 = document.getElementById('tt2_y').value;
        ttz2 = document.getElementById('tt2_z').value;
    })
    var tick = function() {
        currentAngle = animate(currentAngle);
        modelMatrix.setRotate(currentAngle, ttx, tty, ttz);
        //modelMatrix.setTranslate(ttx1, tty1, ttz1); 
        // modelMatrix.setScale(ttx2, tty2, ttz2);
        gl.uniformMatrix4fv(u_ModelMatrix, false, modelMatrix.elements);
        mviewProjMatrix.set(viewProjMatrix).multiply(modelMatrix);
        gl.uniformMatrix4fv(u_MvpMatrix, false, mviewProjMatrix.elements);
        normalMatrix.setInverseOf(modelMatrix);
        normalMatrix.transpose();
        gl.uniformMatrix4fv(u_NormalMatrix, false, normalMatrix.elements);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
        gl.drawElements(gl.TRIANGLES, n, gl.UNSIGNED_BYTE, 0);
        requestAnimationFrame(tick, canvas);
    };
    tick();
}

function initVertexBuffers(gl) {
    var vertices = new Float32Array([
        0.8, 0.4, 0, 0.2, 0.4, 0.8, 0.2, 0.4, -0.8, -0.7, 0.4, 0.5, -0.7, 0.4, -0.5, -0.3, 0.4, 0, -0.1, 0.4, 0.3, -0.1, 0.4, -0.3, 0.2, 0.4, 0.2, 0.2, 0.4, -0.2, -0.8, -0.4, 0, -0.2, -0.4, 0.8, -0.2, -0.4, -0.8, 0.7, -0.4, 0.5, 0.7, -0.4, -0.5,
        0.3, -0.4, 0, 0.1, -0.4, 0.3, 0.1, -0.4, -0.3, -0.2, -0.4, 0.2, -0.2, -0.4, -0.2, -0.5, 0.1, 0, -0.1, 0.1, 0.5, -0.1, 0.1, -0.5, 0.4, 0.1, 0.3, 0.4, 0.1, -0.3,
        0.5, -0.1, 0, 0.1, -0.1, 0.5, 0.1, -0.1, -0.5, -0.4, -0.1, 0.3, -0.4, -0.1, -0.3,
        0, 1, 0, 0, -1, 0
    ]);
    var colors = new Float32Array([1, 0, 0, 1,
        0, 1, 0, 1,
        0, 0, 1, 1,
        1, 0, 0, 1,
        0, 0, 1, 1,
        0, 1, 0, 1,
        1, 0, 0, 1,
        0, 1, 0, 1,
        0, 0, 1, 1,
        1, 0, 0, 1,
        0, 0, 1, 1,
        0, 1, 0, 1
    ]);
    var normals = new Float32Array([
        0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, // v0-v1-v2-v3 front
        1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, // v0-v3-v4-v5 right
        0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, // v0-v5-v6-v1 up
        -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, // v1-v6-v7-v2 left
        0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, // v7-v4-v3-v2 down
        0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0 // v4-v7-v6-v5 back
    ]);
    var indices = new Uint8Array([
        0, 9, 8, 2, 7, 9, 4, 5, 7, 3, 6, 5, 1, 8, 6,
        0, 8, 23, 30, 6, 8, 3, 21, 6, 11, 26, 21, 13, 23, 26,
        2, 9, 24, 30, 8, 9, 1, 23, 8, 13, 25, 23, 14, 24, 25,
        4, 7, 22, 30, 9, 7, 0, 24, 9, 14, 27, 24, 12, 22, 27,
        3, 5, 20, 30, 7, 5, 2, 22, 7, 12, 29, 22, 10, 20, 29,
        1, 6, 21, 30, 5, 6, 4, 20, 5, 10, 28, 20, 11, 21, 28,
        10, 19, 18, 12, 17, 19, 14, 15, 17, 13, 16, 15, 11, 18, 16,
        31, 19, 17, 14, 17, 27, 2, 27, 22, 4, 22, 29, 10, 29, 19,
        31, 18, 19, 12, 19, 29, 4, 29, 20, 3, 20, 28, 11, 28, 18,
        31, 16, 18, 10, 18, 28, 3, 28, 21, 1, 21, 26, 13, 26, 16,
        31, 15, 16, 11, 16, 26, 1, 26, 23, 0, 23, 25, 14, 25, 15,
        31, 17, 15, 13, 15, 25, 0, 25, 24, 2, 24, 27, 12, 27, 17
    ]);
    if (!initArrayBuffer(gl, 'a_Position', vertices, 3, gl.FLOAT)) return -1;
    if (!initArrayBuffer(gl, 'a_Color', colors, 3, gl.FLOAT)) return -1;
    if (!initArrayBuffer(gl, 'a_Normal', normals, 3, gl.FLOAT)) return -1;
    gl.bindBuffer(gl.ARRAY_BUFFER, null);
    var indexBuffer = gl.createBuffer();
    if (!indexBuffer) {
        console.log('Failed to create the buffer object');
        return false;
    }
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, indices, gl.STATIC_DRAW);
    return indices.length;
}

function initArrayBuffer(gl, attribute, data, num, type) {
    var buffer = gl.createBuffer();
    if (!buffer) {
        console.log('Failed to create the buffer object');
        return false;
    }
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(gl.ARRAY_BUFFER, data, gl.STATIC_DRAW);
    var a_attribute = gl.getAttribLocation(gl.program, attribute);
    if (a_attribute < 0) {
        console.log('Failed to get the storage location of ' + attribute);
        return false;
    }
    gl.vertexAttribPointer(a_attribute, num, type, false, 0, 0);
    gl.enableVertexAttribArray(a_attribute);
    return true;
}

function check(gl, n, x, y, currentAngle, u_Clicked, viewProjMatrix, u_MvpMatrix) {
    var picked = false;
    gl.uniform1i(u_Clicked, 1);
    draw(gl, n, currentAngle, viewProjMatrix, u_MvpMatrix);
}
var ANGLE_STEP = 0.0;
var g_last = Date.now();

function animate(angle) {
    var now = Date.now();
    var elapsed = now - g_last;
    g_last = now;
    var newAngle = angle + (ANGLE_STEP * elapsed) / 1000.0;
    return newAngle %= 360;
}

function stop() {
    ANGLE_STEP = ANGLE_STEP * 0;
}

function up() {
    ANGLE_STEP += 10;
}

function down() {
    ANGLE_STEP -= 10;
}



var mouseDown = false;
var lastMouseX = null;
var lastMouseY = null;

var moonRotationMatrix = mat4.create();
mat4.identity(moonRotationMatrix);

function handleMouseDown(event) {
    mouseDown = true;
    lastMouseX = event.clientX;
    lastMouseY = event.clientY;
}


function handleMouseUp(event) {
    mouseDown = false;
}


function handleMouseMove(event) {
    if (!mouseDown) {
        return;
    }
    var newX = event.clientX;
    var newY = event.clientY;

    var deltaX = newX - lastMouseX
    var newRotationMatrix = mat4.create();
    mat4.identity(newRotationMatrix);
    mat4.rotate(newRotationMatrix, degToRad(deltaX / 10), [0, 1, 0]);

    var deltaY = newY - lastMouseY;
    mat4.rotate(newRotationMatrix, degToRad(deltaY / 10), [1, 0, 0]);

    mat4.multiply(newRotationMatrix, modelMatrix, modelMatrix);

    lastMouseX = newX
    lastMouseY = newY;
}
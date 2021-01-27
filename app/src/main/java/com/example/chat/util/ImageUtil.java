package com.example.chat.util;

public class ImageUtil {

    public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
                        + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    public static byte[] rotateYUV420Degree90(byte[] input, int width, int height, int rotation) {
        int frameSize = width * height;
        int qFrameSize = frameSize / 4;
        byte[] output = new byte[frameSize + 2 * qFrameSize];


        boolean swap = (rotation == 90 || rotation == 270);
        boolean yflip = (rotation == 90 || rotation == 180);
        boolean xflip = (rotation == 270 || rotation == 180);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int xo = x, yo = y;
                int w = width, h = height;
                int xi = xo, yi = yo;
                if (swap) {
                    xi = w * yo / h;
                    yi = h * xo / w;
                }
                if (yflip) {
                    yi = h - yi - 1;
                }
                if (xflip) {
                    xi = w - xi - 1;
                }
                output[w * yo + xo] = input[w * yi + xi];
                int fs = w * h;
                int qs = (fs >> 2);
                xi = (xi >> 1);
                yi = (yi >> 1);
                xo = (xo >> 1);
                yo = (yo >> 1);
                w = (w >> 1);
                h = (h >> 1);
// adjust for interleave here
                int ui = fs + (w * yi + xi) * 2;
                int uo = fs + (w * yo + xo) * 2;
// and here
                int vi = ui + 1;
                int vo = uo + 1;
                output[uo] = input[ui];
                output[vo] = input[vi];
            }
        }
        return output;
    }

    public static void mYuvToNv21(byte[] y, byte[] u, byte[] v, byte[] nv21, int width, int height) {
        System.arraycopy(y, 0, nv21, 0, y.length);
        int length = y.length + u.length / 2 + v.length / 2;
        int indexU = 0;
        int indexV = 0;
        for (int i = width * height; i < length; i += 2) {
            nv21[i] = v[indexV];
            nv21[i + 1] = u[indexU];
            indexU += 2;
            indexV += 2;
        }
    }

    public static void yuvToNv21(byte[] y, byte[] u, byte[] v, byte[] nv21, int stride, int height) {
        System.arraycopy(y, 0, nv21, 0, y.length);
        // 注意，若length值为 y.length * 3 / 2 会有数组越界的风险，需使用真实数据长度计算
        int length = y.length + u.length / 2 + v.length / 2;
        int uIndex = 0, vIndex = 0;
        for (int i = stride * height; i < length; i += 2) {
            nv21[i] = v[vIndex];
            nv21[i + 1] = u[uIndex];
            vIndex += 2;
            uIndex += 2;
        }
    }

    public static void nv21_rotate_90(byte[] nv21, byte[] nv21_rotated, int width, int height) {
        int sizeY = width * height;
        int bufferSize = sizeY * 3 / 2;

        int i = 0;
        int startPos = (height - 1) * width;
        for (int x = 0; x < width; x++) {
            int offset = startPos;
            for (int y = height - 1; y >= 0; y--) {
                nv21_rotated[i] = nv21[offset + x];
                i++;
                offset -= width;
            }
        }
        i = bufferSize - 1;
        for (int x = width - 1; x > 0; x -= 2) {
            int offset = sizeY;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21[offset + x];
                i--;
                nv21_rotated[i] = nv21[offset + x - 1];
                i--;
                offset += width;
            }
        }
    }


    public static byte[] nv21ToNv12(byte[] nv21) {
        byte[] nv12 = new byte[nv21.length];
        int len = nv12.length * 2 / 3;
        System.arraycopy(nv21, 0, nv12, 0, len);
        int index = len;
        while (index < nv12.length - 1) {
            nv12[index] = nv21[index + 1];
            nv12[index + 1] = nv21[index];
            index += 2;
        }
        return nv12;
    }

}

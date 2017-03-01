package edu.unh.iol.dlc;

class VNCServerConfig
{
    public final int width;
    public final int height;
    public final String name;
    public final VNCPixelFormat pixelFormat;

    public VNCServerConfig(int width, int height, String name, VNCPixelFormat pixelFormat)
    {
        this.width = width;
        this.height = height;
        this.name = name;
        this.pixelFormat = pixelFormat;
    }
}

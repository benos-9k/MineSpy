package minespy;

public interface IRenderListener {

	public void notifyRenderer(Renderer r);

	public void notifyProgress(int progress, int total);

	public void notifyTermination(Exception te);

}

package minespy.mapshaders;

import minespy.ChunkCoord;
import minespy.IWorld;
import minespy.colorproviders.IBlockColorProvider;

/**
 * A 'shader' used to actually draw the map. A separate instance of the specified shader will be created for each
 * rendering thread.
 * 
 * @author Ben Allen
 * 
 */
public interface IMapShader {

	/**
	 * Compute the color of the map at a global (x, z) block-coordinate.
	 * 
	 * @param w
	 *            The world being drawn.
	 * @param x
	 *            The global x block-coordinate.
	 * @param z
	 *            The global z block-coordinate.
	 * @param block_color_provider
	 *            Specifies what color a block should be.
	 * @param skylight
	 *            The light level provided by the sky (0 to 15 inclusive).
	 * @return The color for this x, z coordinate.
	 */
	public int shade(IWorld w, int x, int z, IBlockColorProvider block_color_provider, int skylight);

	/**
	 * Called before the renderer begins to use this shader to draw the specified chunk, so that this shader may ensure
	 * whatever chunks it needs (including the one being drawn) are loaded.
	 * 
	 * @param w
	 *            The world being drawn.
	 * @param cc
	 *            The chunk-coordinate of the chunk about to be drawn.
	 */
	public void acquireChunks(IWorld w, ChunkCoord cc);

	/**
	 * Called after the renderer has finished using this shader to draw the specified chunk, so that this shader may
	 * release whatever chunks it acquired in <code>acquireChunks()</code>.
	 * 
	 * @param w
	 *            The world being drawn.
	 * @param cc
	 *            The chunk-coordinate of the chunk just finished drawing.
	 */
	public void releaseChunks(IWorld w, ChunkCoord cc);

}

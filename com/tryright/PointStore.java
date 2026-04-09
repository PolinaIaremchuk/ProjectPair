package com.tryright;

/**
 * Interface for accessing point data from various storage formats.
 */
public interface PointStore {
  /**
   * Get X coordinate at given index.
   *
   * @param idx point index
   * @return X coordinate
   */
  int getX(int idx);
  /**
   * Get Y coordinate at given index.
   *
   * @param idx point index
   * @return Y coordinate
   */
  int getY(int idx);
  /**
   * Get total number of points.
   *
   * @return point count
   */
  int numPoints();
  /**
   * Release any resources held by this store.
   */
  void close();
}

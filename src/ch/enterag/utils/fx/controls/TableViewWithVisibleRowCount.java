/* https://stackoverflow.com/questions/26298337/tableview-adjust-number-of-visible-rows/26364210#26364210 */
package ch.enterag.utils.fx.controls;

import java.lang.reflect.*;
import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory; 
import javafx.beans.property.*;
import com.sun.javafx.scene.control.skin.*;

/**
 * TableView with visibleRowCountProperty.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewWithVisibleRowCount<T> extends TableView<T>
{

  private IntegerProperty visibleRowCount = new SimpleIntegerProperty(
    this, "visibleRowCount", 10);

  public IntegerProperty visibleRowCountProperty()
  {
    return visibleRowCount;
  }

  @Override
  protected Skin<?> createDefaultSkin()
  {
    return new TableViewSkinX<T>(this);
  }

  /**
   * Skin that respects table's visibleRowCount property.
   */
  public static class TableViewSkinX<T> extends TableViewSkin<T>
  {

    public TableViewSkinX(TableViewWithVisibleRowCount<T> tableView)
    {
      super(tableView);
      registerChangeListener(tableView.visibleRowCountProperty(),
        "VISIBLE_ROW_COUNT");
      handleControlPropertyChanged("VISIBLE_ROW_COUNT");
    }

    @Override
    protected void handleControlPropertyChanged(String p)
    {
      super.handleControlPropertyChanged(p);
      if ("VISIBLE_ROW_COUNT".equals(p))
      {
        needCellsReconfigured = true;
        getSkinnable().requestFocus();
      }
    }

    /**
     * Returns the visibleRowCount value of the table.
     */
    private int getVisibleRowCount()
    {
      return ((TableViewWithVisibleRowCount<T>) getSkinnable())
        .visibleRowCountProperty().get();
    }

    /**
     * Calculates and returns the pref height of the for the given number of
     * rows.
     * 
     * If flow is of type MyFlow, queries the flow directly otherwise invokes
     * the method.
     */
    protected double getFlowPrefHeight(int rows)
    {
      double height = 0;
      if (flow instanceof MyFlow)
      {
        height = ((MyFlow) flow).getPrefLength(rows);
      }
      else
      {
        for (int i = 0; i < rows && i < getItemCount(); i++)
        {
          height += invokeFlowCellLength(i);
        }
      }
      return height + snappedTopInset() + snappedBottomInset();

    }

    /**
     * Overridden to compute the sum of the flow height and header prefHeight.
     */
    @Override
    protected double computePrefHeight(double width, double topInset,
      double rightInset, double bottomInset, double leftInset)
    {
      // super hard-codes to 400 .. doooh
      double prefHeight = getFlowPrefHeight(getVisibleRowCount());
      return prefHeight + getTableHeaderRow().prefHeight(width);
    }

    /**
     * Reflectively invokes protected getCellLength(i) of flow.
     * @param index
     *          the index of the cell.
     * @return the cell height of the cell at index.
     */
    protected double invokeFlowCellLength(int index)
    {
      double height = 1.0;
      Class<?> clazz = VirtualFlow.class;
      try
      {
        Method method = clazz.getDeclaredMethod("getCellLength",
          Integer.TYPE);
        method.setAccessible(true);
        return ((double) method.invoke(flow, index));
      }
      catch (NoSuchMethodException | SecurityException
        | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e)
      {
        e.printStackTrace();
      }
      return height;
    }

    /**
     * Overridden to return custom flow.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected VirtualFlow createVirtualFlow()
    {
      return new MyFlow();
    }

    /**
     * Extended to expose length calculation per a given # of rows.
     */
    @SuppressWarnings({ "rawtypes" })
    public static class MyFlow extends VirtualFlow
    {
      protected double getPrefLength(int rowsPerPage)
      {
        double sum = 0.0;
        int rows = rowsPerPage; // Math.min(rowsPerPage, getCellCount());
        for (int i = 0; i < rows; i++)
        {
          sum += getCellLength(i);
        }
        return sum;
      }
    }
  }
}
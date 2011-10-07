package org.asoem.greyfish.utils;

import com.explodingpixels.macwidgets.SourceList;
import com.explodingpixels.macwidgets.SourceListCategory;
import com.explodingpixels.macwidgets.SourceListItem;
import com.explodingpixels.macwidgets.SourceListModel;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.KeyListener;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class MacWidgetUtils {

	public static SourceListModel createListModel(SourceListCategory ... categories) {
        checkNotNull(categories);

		final SourceListModel ret = new SourceListModel();
		for (SourceListCategory sourceListCategory : categories) {
            checkNotNull(sourceListCategory);
			ret.addCategory(sourceListCategory);
		}
		return ret;
	}

	public static boolean removeItemFromModel(SourceListModel model, SourceListItem item) {
        checkNotNull(model);
        checkNotNull(item);

		final SourceListCategory cat = getCategory(model, item);
		if (cat != null) {
			model.removeItemFromCategory(item, cat);
			return true;
		}
		return false;
	}

    @Nullable
	public static SourceListCategory getCategory(final SourceListModel model, @Nullable final SourceListItem item) {
        checkNotNull(model);

		List<SourceListCategory> cat = model.getCategories();
		for (SourceListCategory sourceListCategory : cat) {
			if (sourceListCategory.containsItem(item)) {
				return sourceListCategory;
			}
		}
		return null;
	}
	
	public static void addKeyListenerToSourceList(final SourceList list, final KeyListener listener) {
		JScrollPane scrollPane = (JScrollPane) list.getComponent().getComponent(0);
		JTree tree = (JTree) scrollPane.getViewport().getComponent(0);
		tree.addKeyListener(listener);
	}

	public static void removeItemsFromCategory(SourceListModel model,
			SourceListCategory ... categories) {
		for (SourceListCategory category : categories) {
			while (category.getItemCount() != 0) {
				SourceListItem item = category.getItems().get(0);
				model.removeItemFromCategory(item, category);
			}
		}
	}
}

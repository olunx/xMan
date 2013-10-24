package com.xxdroid.xman.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.xxdroid.xman.R;
import com.xxdroid.xman.ui.FileExplorerTabActivity;
import com.xxdroid.xman.ui.FileViewActivity;
import com.xxdroid.xman.helper.FileIconHelper;

import com.xxdroid.xman.listener.FileViewInteractionHub;
import com.xxdroid.xman.listener.FileViewInteractionHub.Mode;

public class FileListItem {
    public static void setupFileListItemInfo(Context context, View view,
            FileInfo fileInfo, FileIconHelper fileIcon,
            FileViewInteractionHub fileViewInteractionHub) {

        // if in moving mode, show selected file always
        if (fileViewInteractionHub.isMoveState()) {
            fileInfo.Selected = fileViewInteractionHub.isFileSelected(fileInfo.filePath);
        }

        ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
        if (fileViewInteractionHub.getMode() == Mode.Pick) {
            checkbox.setVisibility(View.GONE);
        } else {
            checkbox.setVisibility(fileViewInteractionHub.canShowCheckBox() ? View.VISIBLE : View.GONE);
            checkbox.setImageResource(fileInfo.Selected ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            checkbox.setTag(fileInfo);
            view.setSelected(fileInfo.Selected);
        }

        Util.setText(view, R.id.file_name, fileInfo.fileName);
        Util.setText(view, R.id.file_count, fileInfo.IsDir ? "(" + fileInfo.Count + ")" : "");
        Util.setText(view, R.id.modified_time, Util.formatDateString(context, fileInfo.ModifiedDate));
        Util.setText(view, R.id.file_size, (fileInfo.IsDir ? "" : Util.convertStorage(fileInfo.fileSize)));

        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
        ImageView lFileImageFrame = (ImageView) view.findViewById(R.id.file_image_frame);

        if (fileInfo.IsDir) {
            lFileImageFrame.setVisibility(View.GONE);
            lFileImage.setImageResource(R.drawable.folder);
        } else {
            fileIcon.setIcon(fileInfo, lFileImage, lFileImageFrame);
        }
    }

    public static class FileItemOnClickListener implements OnClickListener {
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        public FileItemOnClickListener(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public void onClick(View v) {
            ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
            assert (img != null && img.getTag() != null);

            FileInfo tag = (FileInfo) img.getTag();
            tag.Selected = !tag.Selected;
            ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
            if (actionMode == null) {
                actionMode = ((FileExplorerTabActivity) mContext)
                        .startActionMode(new ModeCallback(mContext,
                                mFileViewInteractionHub));
                ((FileExplorerTabActivity) mContext).setActionMode(actionMode);
            } else {
                actionMode.invalidate();
            }
            if (mFileViewInteractionHub.onCheckItem(tag, v)) {
                img.setImageResource(tag.Selected ? R.drawable.btn_check_on_holo_light
                        : R.drawable.btn_check_off_holo_light);
            } else {
                tag.Selected = !tag.Selected;
            }
            Util.updateActionModeTitle(actionMode, mContext,
                    mFileViewInteractionHub.getSelectedFileList().size());
        }
    }

    public static class ModeCallback implements ActionMode.Callback {
        private Menu mMenu;
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        private void initMenuItemSelectAllOrCancel() {
            boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
            mMenu.findItem(R.id.action_cancel).setVisible(isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(!isSelectedAll);
        }

        private void scrollToSDcardTab() {
            ActionBar bar = ((FileExplorerTabActivity) mContext).getActionBar();
            if (bar.getSelectedNavigationIndex() != Util.SDCARD_TAB_INDEX) {
                bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
            }
        }

        public ModeCallback(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            mMenu = menu;
            inflater.inflate(R.menu.operation_menu, mMenu);
            initMenuItemSelectAllOrCancel();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mMenu.findItem(R.id.action_copy_path).setVisible(
                    mFileViewInteractionHub.getSelectedFileList().size() == 1);
            mMenu.findItem(R.id.action_cancel).setVisible(
            		mFileViewInteractionHub.isSelected());
            mMenu.findItem(R.id.action_select_all).setVisible(
            		!mFileViewInteractionHub.isSelectedAll());
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    mFileViewInteractionHub.onOperationDelete();
                    mode.finish();
                    break;
                case R.id.action_copy:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .copyFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_move:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .moveToFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_send:
                    mFileViewInteractionHub.onOperationSend();
                    mode.finish();
                    break;
                case R.id.action_copy_path:
                    mFileViewInteractionHub.onOperationCopyPath();
                    mode.finish();
                    break;
                case R.id.action_cancel:
                    mFileViewInteractionHub.clearSelection();
                    initMenuItemSelectAllOrCancel();
                    mode.finish();
                    break;
                case R.id.action_select_all:
                    mFileViewInteractionHub.onOperationSelectAll();
                    initMenuItemSelectAllOrCancel();
                    break;
            }
            Util.updateActionModeTitle(mode, mContext, mFileViewInteractionHub
                    .getSelectedFileList().size());
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFileViewInteractionHub.clearSelection();
            ((FileExplorerTabActivity) mContext).setActionMode(null);
        }
    }
}

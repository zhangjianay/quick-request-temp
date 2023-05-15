/*
 * Copyright 2021 kings1990(darkings1990@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.kings1990.plugin.fastrequest.action;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import io.github.kings1990.plugin.fastrequest.util.MyResourceBundleUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CheckBoxFilterAction<T> extends AnAction {
    private JBPopup myPopup;
    private List<JCheckBox> checkBoxList;
    private final Filter<T> filter;
    private final Runnable rebuildRunnable;

    public CheckBoxFilterAction(String title, String description, Icon icon, Filter<T> filter, Runnable rebuildRunnable) {
        super(title, description, icon);
        this.filter = filter;
        this.rebuildRunnable = rebuildRunnable;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JPanel panel = initView(filter, rebuildRunnable);
        createPopup(e, panel);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Icon icon = getTemplatePresentation().getIcon();
        boolean isActive = filter.getAllElements().size() != filter.getSelectedElementList().size();
        boolean isSelected = myPopup != null && !myPopup.isDisposed();
        e.getPresentation().setIcon(isActive ? ExecutionUtil.getLiveIndicator(icon) : icon);
        e.getPresentation().setEnabled(true);
        Toggleable.setSelected(e.getPresentation(), isSelected);
    }

    public void createPopup(AnActionEvent e, JPanel contentPanel) {
        JBPopupListener popupCloseListener = new JBPopupListener() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                checkBoxList = null;
                myPopup = null;
            }
        };
        myPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(contentPanel, null)
                .setModalContext(false)
                .setFocusable(false)
                .setResizable(true)
                .setCancelOnClickOutside(true)
                .setMinSize(new Dimension(200, 200))
                .addListener(popupCloseListener)
                .createPopup();
        myPopup.showUnderneathOf(e.getInputEvent().getComponent());
    }

    private JPanel initView(Filter<T> filter, Runnable rebuildRunnable) {
        checkBoxList = new ArrayList<>();
        List<T> elementList = filter.getAllElements();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel checkboxPane = new JPanel();
        List<T> selectedElementList = filter.getSelectedElementList();
        checkboxPane.setLayout(new BoxLayout(checkboxPane, BoxLayout.Y_AXIS));
        JPanel buttonPane = new JPanel();
        for (T element : elementList) {
            String text = filter.getElementText(element);
            Icon icon = filter.getElementIcon(element);
            JCheckBox checkBox = new JCheckBox(text, selectedElementList.contains(element));
            checkBox.addActionListener(e -> {
                filter.setSelected(element, checkBox.isSelected());
                rebuildRunnable.run();
            });
            checkBoxList.add(checkBox);
            if (icon == null) {
                checkboxPane.add(checkBox);
            } else {
                checkBox.setText("");
                checkboxPane.add(JBUI.Panels.simplePanel().addToLeft(checkBox).addToCenter(new JLabel(text, icon, JLabel.LEFT)).withPreferredHeight(24).withMaximumHeight(24));
            }

        }


        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(checkboxPane);
        scrollPane.setMinimumSize(new Dimension(200, 200));
        scrollPane.setPreferredSize(new Dimension(200, 200));

        JButton all = new JButton(MyResourceBundleUtil.getKey("filter.all"));
        all.addActionListener(e -> {
            for (int i = 0; i < checkBoxList.size(); i++) {
                checkBoxList.get(i).setSelected(true);
                filter.setSelected(elementList.get(i), true);
            }
            rebuildRunnable.run();
        });
        buttonPane.add(all);

        JButton none = new JButton(MyResourceBundleUtil.getKey("filter.none"));
        none.addActionListener(e -> {
            for (int i = 0; i < checkBoxList.size(); i++) {
                checkBoxList.get(i).setSelected(false);
                filter.setSelected(elementList.get(i), false);
            }
            rebuildRunnable.run();
        });
        buttonPane.add(none);

        JButton invert = new JButton(MyResourceBundleUtil.getKey("filter.invert"));
        invert.addActionListener(e -> {
            for (int i = 0; i < checkBoxList.size(); i++) {
                boolean selected = checkBoxList.get(i).isSelected();
                checkBoxList.get(i).setSelected(!selected);
                filter.setSelected(elementList.get(i), !selected);
            }
            rebuildRunnable.run();
        });
        buttonPane.add(invert);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPane, BorderLayout.SOUTH);

        return panel;
    }


    public static class Filter<T> {
        private final List<T> myElementList;
        private final Function<? super T, @Nls String> myTextExtractor;
        private final Function<? super T, ? extends Icon> myIconExtractor;
        private final ChooseByNameFilterConfiguration<? super T> myPersistentConfiguration;

        public List<T> getAllElements() {
            return myElementList;
        }

        public Filter(List<T> myElementList, Function<? super T, @Nls String> textExtractor, Function<? super T, ? extends Icon> iconExtractor, ChooseByNameFilterConfiguration<? super T> myPersistentConfiguration) {
            this.myElementList = myElementList;
            this.myTextExtractor = textExtractor;
            this.myIconExtractor = iconExtractor;
            this.myPersistentConfiguration = myPersistentConfiguration;
        }

        public String getElementText(T element) {
            return myTextExtractor.apply(element);
        }

        public Icon getElementIcon(T element) {
            return myIconExtractor.apply(element);
        }

        public List<T> getSelectedElementList() {
            return ContainerUtil.filter(myElementList, myPersistentConfiguration::isFileTypeVisible);
        }

        public boolean isSelected(T element) {
            return myPersistentConfiguration.isFileTypeVisible(element);
        }

        public void setSelected(T element, boolean selected) {
            myPersistentConfiguration.setVisible(element, selected);
        }
    }
}

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

package io.github.kings1990.plugin.fastrequest.configurable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.intellij.ui.CollectionListModel;
import com.intellij.util.ui.ListTableModel;
import io.github.kings1990.plugin.fastrequest.config.Constant;
import io.github.kings1990.plugin.fastrequest.config.FastRequestComponent;
import io.github.kings1990.plugin.fastrequest.model.DataMapping;
import io.github.kings1990.plugin.fastrequest.model.FastRequestConfiguration;
import io.github.kings1990.plugin.fastrequest.util.KV;
import io.github.kings1990.plugin.fastrequest.view.AbstractConfigurableView;
import io.github.kings1990.plugin.fastrequest.view.sub.DataMappingConfigViewNew;
import org.apache.commons.collections.ListUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;

public class DataMappingConfigurable extends AbstractConfigConfigurable {
    protected FastRequestConfiguration config;
    private final DataMappingConfigViewNew view;

    public DataMappingConfigurable() {
        config = FastRequestComponent.getInstance().getState();
        view = new DataMappingConfigViewNew(config);
    }

    @Override
    public AbstractConfigurableView getView() {
        return view;
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        return view.getComponent();
    }

    @Override
    public String getDisplayName() {
        return ResourceBundle.getBundle(Constant.I18N_PATH).getString("DateTypeConfig");
    }

    @Override
    public boolean isModified() {
        List<DataMapping> currentCustomDataMappingList = view.getViewCustomDataMappingList();
        List<DataMapping> customDataMappingList = config.getCustomDataMappingList();
        List<DataMapping> currentDefaultDataMappingList = view.getViewDefaultDataMappingList();
        List<DataMapping> defaultDataMappingList = config.getDefaultDataMappingList();

        Integer randomStringLength = config.getRandomStringLength();
        Integer viewRandomStringLength = Integer.parseInt(view.getRandomStringTextField().getText());

        String randomStringStrategy = config.getRandomStringStrategy();
        String viewRandomStringStrategy = (String) view.getRandomStringStrategyComboBox().getSelectedItem();

        String randomStringDelimiter = config.getRandomStringDelimiter();
        String viewRandomStringDelimiter = view.getRandomStringDelimiterTextField().getText();

        List<String> ignoreDataMappingList = config.getIgnoreDataMappingList();
        List<String> viewIgnoreDataMappingList = view.getViewIgnoreDateMappingList();

        return !randomStringDelimiter.equals(viewRandomStringDelimiter) ||
                !randomStringStrategy.equals(viewRandomStringStrategy) ||
                !randomStringLength.equals(viewRandomStringLength) ||
                !judgeEqual(currentCustomDataMappingList, customDataMappingList) ||
                !judgeEqual(currentDefaultDataMappingList, defaultDataMappingList) ||
                !ListUtils.isEqualList(ignoreDataMappingList, viewIgnoreDataMappingList)
                ;
    }

    @Override
    public void apply() {
        List<DataMapping> viewCustomDataMappingList = view.getViewCustomDataMappingList();
        List<DataMapping> changeCustomDataMappingList = JSONArray.parseArray(JSON.toJSONString(viewCustomDataMappingList), DataMapping.class);
        config.setCustomDataMappingList(changeCustomDataMappingList);

        List<DataMapping> viewDefaultDataMappingList = view.getViewDefaultDataMappingList();
        List<DataMapping> changeDefaultDataMappingList = JSONArray.parseArray(JSON.toJSONString(viewDefaultDataMappingList), DataMapping.class);
        config.setDefaultDataMappingList(changeDefaultDataMappingList);

        int viewRandomStringLength = Integer.parseInt(view.getRandomStringTextField().getText());
        config.setRandomStringLength(viewRandomStringLength);

        String viewRandomStringStrategy = (String) view.getRandomStringStrategyComboBox().getSelectedItem();
        config.setRandomStringStrategy(viewRandomStringStrategy);

        String viewRandomStringDelimiter = view.getRandomStringDelimiterTextField().getText();
        config.setRandomStringDelimiter(viewRandomStringDelimiter);

        List<String> viewIgnoreDataMappingList = view.getViewIgnoreDateMappingList();
        List<String> changeIgnoreDataMappingList = JSONArray.parseArray(JSON.toJSONString(viewIgnoreDataMappingList), String.class);
        config.setIgnoreDataMappingList(changeIgnoreDataMappingList);
        KV.changeConfig();
    }

    @Override
    public void reset() {
        super.reset();
        List<DataMapping> oldCustomDataMappingList = config.getCustomDataMappingList();
        List<DataMapping> oldCustomDataMappingListNew = JSONArray.parseArray(JSON.toJSONString(oldCustomDataMappingList), DataMapping.class);
        List<DataMapping> oldDefaultDataMappingList = config.getDefaultDataMappingList();
        List<DataMapping> oldDefaultDataMappingListNew = JSONArray.parseArray(JSON.toJSONString(oldDefaultDataMappingList), DataMapping.class);
        List<String> oldIgnoreDataMappingList = config.getIgnoreDataMappingList();
        List<String> oldIgnoreDataMappingListNew = JSONArray.parseArray(JSON.toJSONString(oldIgnoreDataMappingList), String.class);
        int randomStringLength = config.getRandomStringLength();
        String randomStringStrategy = config.getRandomStringStrategy();
        String randomStringDelimiter = config.getRandomStringDelimiter();
        view.setViewCustomDataMappingList(oldCustomDataMappingListNew);
        view.setViewDefaultDataMappingList(oldDefaultDataMappingListNew);
        view.getCustomTable().setModel(new ListTableModel<>(view.getColumnInfo(), oldCustomDataMappingListNew));
        view.getDefaultDataMappingTable().setModel(new ListTableModel<>(view.getColumnInfo(), oldDefaultDataMappingListNew));
        view.getRandomStringTextField().setText(randomStringLength + "");
        view.getRandomStringStrategyComboBox().setSelectedItem(randomStringStrategy);
        view.getRandomStringDelimiterTextField().setText(randomStringDelimiter);
        view.setViewIgnoreDateMappingList(oldIgnoreDataMappingListNew);
        view.getIgnoreDateMappingJbList().setModel(new CollectionListModel<>(oldIgnoreDataMappingListNew));
    }

    public boolean judgeEqual(List<DataMapping> list1, List<DataMapping> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            DataMapping dataMapping1 = list1.get(i);
            DataMapping dataMapping2 = list2.get(i);
            if (!dataMapping1.getType().equals(dataMapping2.getType()) || !dataMapping1.getValue().equals(dataMapping2.getValue())) {
                return false;
            }
        }
        return true;
    }

}

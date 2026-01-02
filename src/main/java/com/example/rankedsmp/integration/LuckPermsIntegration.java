package com.example.rankedsmp.integration;

import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.cacheddata.Result;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.metadata.NodeMetadataKey;
import net.luckperms.api.node.types.PrefixNode;

public class LuckPermsIntegration {
    private static final NodeMetadataKey<Boolean> RANKED_PREFIX_KEY =
            NodeMetadataKey.of("rankedsmp-resolved-prefix", Boolean.class);

    private final LuckPerms api;

    public LuckPermsIntegration() {
        this.api = LuckPermsProvider.get();
    }

    public void updateUserPrefix(UUID uuid, String rankLabel) {
        User user = api.getUserManager().getUser(uuid);
        if (user == null) {
            return;
        }

        user.data().clear(node -> node.getMetadata(RANKED_PREFIX_KEY).orElse(false));

        CachedMetaData metaData = user.getCachedData().getMetaData();
        Result<String, PrefixNode> prefixResult = metaData.queryPrefix();
        String prefix = prefixResult.result();
        PrefixNode prefixNode = prefixResult.node();
        if (prefix == null || prefixNode == null || !prefix.contains("%rank%")) {
            api.getUserManager().saveUser(user);
            return;
        }

        String resolvedPrefix = prefix.replace("%rank%", rankLabel);
        PrefixNode resolvedNode = PrefixNode.builder(resolvedPrefix, prefixNode.getPriority() + 1)
                .withMetadata(RANKED_PREFIX_KEY, true)
                .build();

        user.data().add(resolvedNode);
        api.getUserManager().saveUser(user);
    }
}

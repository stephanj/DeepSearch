package com.devoxx.agentic.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ContentStoreTest {

    @Test
    void shouldAddAndRetrieveLinks() {
        ContentStore store = new ContentStore();
        List<String> links = Arrays.asList("link1", "link2");

        store.addLinks("query1", "google", links);

        assertEquals(links, store.getLinksByEngine().get("google"));
    }

    @Test
    void shouldAddAndRetrieveContent() {
        ContentStore store = new ContentStore();
        store.addLinks("query1", "google", Arrays.asList("link1"));
        store.addContent("query1", "google", "link1", "content1");

        String allContent = store.getAllContent();
        assertTrue(allContent.contains("content1"));
    }
}
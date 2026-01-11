package dev.studylink.studylink.business;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.dao.ResourceFactory;
import dev.studylink.studylink.exception.InvalidResourceDataException;
import dev.studylink.studylink.exception.ResourceNotFoundException;
import dev.studylink.studylink.exception.UnauthorizedResourceAccessException;
import dev.studylink.studylink.impl.db.mysql.MySQLCategoryDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLResourceFactory;

public class ResourceFacade {
    private static ResourceFacade instance = null;
    private ResourceManager resourceManager;
    private SessionFacade sessionFacade;
    private ResourceFactory resourceFactory;

    private static final String UPLOAD_DIRECTORY = "uploads/resources/";

    private ResourceFacade() {
        this.resourceFactory = MySQLResourceFactory.getInstance();
        CategoryDAO categoryDAO = MySQLCategoryDAO.getInstance();
        this.resourceManager = ResourceManager.getInstance(resourceFactory, categoryDAO);
        this.sessionFacade = SessionFacade.getInstance();

        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }

    public static ResourceFacade getInstance() {
        if (instance == null) {
            instance = new ResourceFacade();
        }
        return instance;
    }

    // RESOURCE CREATION & MANAGEMENT

    public Resource createResourcePost(String title, String content, List<Category> categories,
                                       File attachmentFile, double price) throws InvalidResourceDataException {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            throw new InvalidResourceDataException("User not logged in");
        }

        String attachmentPath = null;
        if (attachmentFile != null) {
            attachmentPath = uploadFile(attachmentFile);
        }

        return resourceManager.createResource(title, content, currentUser.getId(),
                categories, attachmentPath, price);
    }

    public boolean updateResourcePost(int resourceId, String title, String content,
                                      List<Category> categories) throws ResourceNotFoundException, UnauthorizedResourceAccessException {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        Resource resource = resourceManager.getResourceById(resourceId);

        if (resource.getOwnerId() != currentUser.getId()) {
            throw new UnauthorizedResourceAccessException("You are not authorized to update this resource");
        }

        resource.setTitle(title);
        resource.setContent(content);

        boolean updated = resourceManager.updateResource(resource);

        if (updated && categories != null) {
            // Update categories
            List<Category> currentCategories = resource.getCategories();

            // Remove old categories
            for (Category cat : currentCategories) {
                if (!categories.contains(cat)) {
                    resourceManager.removeCategoryFromResource(resourceId, cat.getId());
                }
            }

            // Add new categories
            for (Category cat : categories) {
                if (!currentCategories.contains(cat)) {
                    resourceManager.addCategoryToResource(resourceId, cat.getId());
                }
            }
        }

        return updated;
    }

    public boolean deleteResourcePost(int resourceId) throws ResourceNotFoundException, UnauthorizedResourceAccessException {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return resourceManager.deleteResource(resourceId, currentUser.getId());
    }

    public Resource getResourceById(int resourceId) throws ResourceNotFoundException {
        return resourceManager.getResourceById(resourceId);
    }

    public List<Resource> getResourceFeed() {
        return resourceManager.getAllResources();
    }

    public List<Resource> searchResources(String query) {
        return resourceManager.searchResources(query);
    }

    public List<Resource> getResourcesByCategory(int categoryId) {
        return resourceManager.getResourcesByCategory(categoryId);
    }

    public List<Resource> getMyResources() {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }

        return resourceManager.getResourcesByOwner(currentUser.getId());
    }

    //  COMMENT MANAGEMENT

    public Comment addCommentToResource(int resourceId, String content) throws ResourceNotFoundException, InvalidResourceDataException {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            throw new InvalidResourceDataException("User not logged in");
        }

        // Check if resource exists
        resourceManager.getResourceById(resourceId);

        return resourceManager.addComment(resourceId, currentUser.getId(), content);
    }

    public boolean deleteComment(int commentId) throws UnauthorizedResourceAccessException {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return resourceManager.deleteComment(commentId, currentUser);
    }

    public List<Comment> getResourceComments(int resourceId) throws ResourceNotFoundException {
        // Check if resource exists
        resourceManager.getResourceById(resourceId);

        return resourceManager.getCommentsForResource(resourceId);
    }

    // ===== SAVE/UNSAVE RESOURCES =====

    public boolean saveResource(int resourceId) throws ResourceNotFoundException {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Check if resource exists
        resourceManager.getResourceById(resourceId);

        return resourceManager.saveResource(currentUser.getId(), resourceId);
    }

    public boolean unsaveResource(int resourceId) throws ResourceNotFoundException {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Check if resource exists
        resourceManager.getResourceById(resourceId);

        return resourceManager.unsaveResource(currentUser.getId(), resourceId);
    }

    public List<Resource> getMySavedResources() {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }

        return resourceManager.getSavedResources(currentUser.getId());
    }

    public boolean isResourceSaved(int resourceId) {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return resourceManager.isResourceSaved(currentUser.getId(), resourceId);
    }

    public List<Resource> getResourcesByOwner(int userId) {
        return resourceManager.getResourcesByOwner(userId);
    }

    //  VIEW TRACKING

    public void viewResource(int resourceId) {
        User currentUser = sessionFacade.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        // Check if user has already viewed this resource
        if (!resourceManager.hasUserViewedResource(currentUser.getId(), resourceId)) {
            // Record the view and increment counter
            resourceManager.recordResourceView(currentUser.getId(), resourceId);
            resourceManager.incrementViewCount(resourceId);
        }
    }

    //  FILE UPLOAD

    private String uploadFile(File file) {
        try {
            // Generate unique filename
            String originalFilename = file.getName();
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path targetPath = Paths.get(UPLOAD_DIRECTORY + uniqueFilename);

            // Copy file
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return UPLOAD_DIRECTORY + uniqueFilename;

        } catch (IOException e) {
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

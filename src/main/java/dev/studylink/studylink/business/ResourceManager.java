package dev.studylink.studylink.business;

import java.util.List;

import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.dao.CommentDAO;
import dev.studylink.studylink.dao.ResourceDAO;
import dev.studylink.studylink.dao.ResourceFactory;
import dev.studylink.studylink.dao.SavedResourceDAO;
import dev.studylink.studylink.exception.InvalidResourceDataException;
import dev.studylink.studylink.exception.ResourceNotFoundException;
import dev.studylink.studylink.exception.UnauthorizedResourceAccessException;

public class ResourceManager {
    private static ResourceManager instance;
    private ResourceFactory resourceFactory;
    private ResourceDAO resourceDAO;
    private CommentDAO commentDAO;
    private SavedResourceDAO savedResourceDAO;
    private CategoryDAO categoryDAO;

    private ResourceManager(ResourceFactory resourceFactory, CategoryDAO categoryDAO) {
        this.resourceFactory = resourceFactory;
        this.resourceDAO = resourceFactory.createResourceDAO();
        this.commentDAO = resourceFactory.createCommentDAO();
        this.savedResourceDAO = resourceFactory.createSavedResourceDAO();
        this.categoryDAO = categoryDAO;
    }

    public static ResourceManager getInstance(ResourceFactory resourceFactory, CategoryDAO categoryDAO) {
        if (instance == null) {
            instance = new ResourceManager(resourceFactory, categoryDAO);
        }
        return instance;
    }

    // ===== RESOURCE CRUD =====

    public Resource createResource(String title, String content, int ownerId,
                                   List<Category> categories, String attachmentPath,
                                   double price) throws InvalidResourceDataException {
        validateResourceData(title, content);

        Resource resource = new Resource(title, content, ownerId);
        resource.setAttachmentPath(attachmentPath);
        resource.setPrice(price);

        boolean created = resourceDAO.createResource(resource);

        if (created) {
            // Add categories
            if (categories != null && !categories.isEmpty()) {
                for (Category category : categories) {
                    resourceDAO.addCategoryToResource(resource.getId(), category.getId());
                }
            }

            // Fetch complete resource with categories
            return resourceDAO.findById(resource.getId())
                    .orElseThrow(() -> new InvalidResourceDataException("Failed to retrieve created resource"));
        }

        throw new InvalidResourceDataException("Failed to create resource");
    }

    public boolean updateResource(Resource resource) throws ResourceNotFoundException {
        if (!resourceDAO.findById(resource.getId()).isPresent()) {
            throw new ResourceNotFoundException("Resource not found with ID: " + resource.getId());
        }

        return resourceDAO.updateResource(resource);
    }

    public boolean deleteResource(int resourceId, int userId) throws ResourceNotFoundException, UnauthorizedResourceAccessException {
        Resource resource = resourceDAO.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));

        if (resource.getOwnerId() != userId) {
            throw new UnauthorizedResourceAccessException("You are not authorized to delete this resource");
        }

        return resourceDAO.deleteResource(resourceId);
    }

    public Resource getResourceById(int resourceId) throws ResourceNotFoundException {
        return resourceDAO.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));
    }

    public List<Resource> getAllResources() {
        return resourceDAO.getAllResources();
    }

    public List<Resource> getResourcesByCategory(int categoryId) {
        return resourceDAO.getResourcesByCategory(categoryId);
    }

    public List<Resource> getResourcesByOwner(int userId) {
        return resourceDAO.getResourcesByOwner(userId);
    }

    public List<Resource> searchResources(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllResources();
        }
        return resourceDAO.searchResources(query);
    }

    // ===== CATEGORY MANAGEMENT FOR RESOURCES =====

    public boolean addCategoryToResource(int resourceId, int categoryId) {
        return resourceDAO.addCategoryToResource(resourceId, categoryId);
    }

    public boolean removeCategoryFromResource(int resourceId, int categoryId) {
        return resourceDAO.removeCategoryFromResource(resourceId, categoryId);
    }

    public List<Category> getResourceCategories(int resourceId) {
        return resourceDAO.getResourceCategories(resourceId);
    }

    // ===== COMMENT MANAGEMENT =====

    public Comment addComment(int resourceId, int userId, String content) throws InvalidResourceDataException {
        validateCommentContent(content);

        Comment comment = new Comment(content, userId, resourceId);
        return commentDAO.createComment(comment);
    }

    public boolean deleteComment(int commentId, User currentUser) throws UnauthorizedResourceAccessException {
        Comment comment = commentDAO.findById(commentId)
                .orElse(null);

        if (comment == null) {
            return false;
        }

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (comment.getAuthorId() != currentUser.getId() && !isAdmin) {
            throw new UnauthorizedResourceAccessException("You are not authorized to delete this comment");
        }

        return commentDAO.deleteComment(commentId);
    }

    public List<Comment> getCommentsForResource(int resourceId) {
        return commentDAO.getCommentsByResource(resourceId);
    }

    // ===== SAVE/UNSAVE RESOURCES =====

    public boolean saveResource(int userId, int resourceId) {
        if (savedResourceDAO.isResourceSaved(userId, resourceId)) {
            return false; // Already saved
        }

        boolean saved = savedResourceDAO.saveResource(userId, resourceId);
        if (saved) {
            resourceDAO.incrementSaveCount(resourceId);
        }
        return saved;
    }

    public boolean unsaveResource(int userId, int resourceId) {
        boolean unsaved = savedResourceDAO.unsaveResource(userId, resourceId);
        if (unsaved) {
            resourceDAO.decrementSaveCount(resourceId);
        }
        return unsaved;
    }

    public List<Resource> getSavedResources(int userId) {
        return savedResourceDAO.getSavedResources(userId);
    }

    public boolean isResourceSaved(int userId, int resourceId) {
        return savedResourceDAO.isResourceSaved(userId, resourceId);
    }

    // ===== VIEW TRACKING =====

    public void incrementViewCount(int resourceId) {
        resourceDAO.incrementViewCount(resourceId);
    }

    public boolean hasUserViewedResource(int userId, int resourceId) {
        return resourceDAO.hasUserViewedResource(userId, resourceId);
    }

    public boolean recordResourceView(int userId, int resourceId) {
        return resourceDAO.recordResourceView(userId, resourceId);
    }

    // ===== VALIDATION =====

    private void validateResourceData(String title, String content) throws InvalidResourceDataException {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidResourceDataException("Title cannot be empty");
        }

        if (title.length() > 200) {
            throw new InvalidResourceDataException("Title cannot exceed 200 characters");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new InvalidResourceDataException("Content cannot be empty");
        }
    }

    private void validateCommentContent(String content) throws InvalidResourceDataException {
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidResourceDataException("Comment cannot be empty");
        }

        if (content.length() < 1 || content.length() > 500) {
            throw new InvalidResourceDataException("Comment must be between 1 and 500 characters");
        }

        // Basic forbidden words filter ( we can add others , this was just for the test)
        String[] forbiddenWords = {"spam", "hack", "cheat"};
        String lowerContent = content.toLowerCase();
        for (String word : forbiddenWords) {
            if (lowerContent.contains(word)) {
                throw new InvalidResourceDataException("Comment contains forbidden words");
            }
        }
    }

    private boolean checkResourceOwnership(int resourceId, int userId) throws UnauthorizedResourceAccessException {
        try {
            Resource resource = getResourceById(resourceId);
            if (resource.getOwnerId() != userId) {
                throw new UnauthorizedResourceAccessException("You are not the owner of this resource");
            }
            return true;
        } catch (ResourceNotFoundException e) {
            throw new UnauthorizedResourceAccessException("Resource not found");
        }
    }
}

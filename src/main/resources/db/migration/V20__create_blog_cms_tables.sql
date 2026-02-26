-- V20__create_blog_cms_tables.sql
-- Blog/CMS System with Rich Content Support

-- ========== ENUMS ==========

CREATE TYPE blog_post_status AS ENUM ('DRAFT', 'PUBLISHED', 'SCHEDULED', 'ARCHIVED');

CREATE TYPE blog_link_type AS ENUM ('PRODUCT', 'SERVICE', 'BUNDLE', 'PROMOTION');

CREATE TYPE blog_video_provider AS ENUM ('YOUTUBE', 'VIMEO', 'CUSTOM');

-- ========== BLOG CATEGORIES ==========

CREATE TABLE blog_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    parent_id UUID REFERENCES blog_categories(id) ON DELETE SET NULL,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_categories_slug ON blog_categories(slug);
CREATE INDEX idx_blog_categories_parent ON blog_categories(parent_id);
CREATE INDEX idx_blog_categories_active ON blog_categories(is_active);

-- ========== BLOG TAGS ==========

CREATE TABLE blog_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_tags_slug ON blog_tags(slug);

-- ========== BLOG POSTS ==========

CREATE TABLE blog_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(300) NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    excerpt VARCHAR(500),
    content TEXT NOT NULL,

    -- Featured image
    featured_image_url VARCHAR(500),
    featured_image_alt TEXT,

    -- Category
    category_id UUID REFERENCES blog_categories(id) ON DELETE SET NULL,

    -- Author
    author_id UUID NOT NULL REFERENCES users(id),

    -- Status & Publishing
    status blog_post_status NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    scheduled_publish_at TIMESTAMP,

    -- Engagement
    view_count INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    allow_comments BOOLEAN DEFAULT TRUE,

    -- SEO
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    meta_keywords TEXT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_posts_slug ON blog_posts(slug);
CREATE INDEX idx_blog_posts_status ON blog_posts(status);
CREATE INDEX idx_blog_posts_category ON blog_posts(category_id);
CREATE INDEX idx_blog_posts_author ON blog_posts(author_id);
CREATE INDEX idx_blog_posts_published ON blog_posts(published_at);
CREATE INDEX idx_blog_posts_featured ON blog_posts(is_featured);

-- ========== BLOG POST TAGS (Many-to-Many) ==========

CREATE TABLE blog_post_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES blog_tags(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, tag_id)
);

CREATE INDEX idx_blog_post_tags_post ON blog_post_tags(post_id);
CREATE INDEX idx_blog_post_tags_tag ON blog_post_tags(tag_id);

-- ========== BLOG POST IMAGES ==========

CREATE TABLE blog_post_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text TEXT,
    caption TEXT,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_post_images_post ON blog_post_images(post_id);

-- ========== BLOG POST VIDEOS ==========

CREATE TABLE blog_post_videos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,

    -- Video details
    video_url VARCHAR(500) NOT NULL,
    video_provider blog_video_provider NOT NULL DEFAULT 'YOUTUBE',
    video_id VARCHAR(200), -- YouTube/Vimeo video ID

    -- Display
    title VARCHAR(300),
    description TEXT,
    thumbnail_url VARCHAR(500),
    display_order INT DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_post_videos_post ON blog_post_videos(post_id);

-- ========== BLOG POST LINKS (Products/Services/Bundles/Promotions) ==========

CREATE TABLE blog_post_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,

    -- Link details
    link_type blog_link_type NOT NULL,
    link_id UUID NOT NULL, -- ID of product/service/bundle/promotion

    -- Display
    display_text VARCHAR(300), -- Optional custom text
    display_order INT DEFAULT 0,

    -- Context
    context TEXT, -- Where in the post this link appears

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_post_links_post ON blog_post_links(post_id);
CREATE INDEX idx_blog_post_links_type ON blog_post_links(link_type);
CREATE INDEX idx_blog_post_links_target ON blog_post_links(link_id);

-- ========== BLOG COMMENTS (Optional) ==========

CREATE TABLE blog_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,

    -- Author (can be customer or guest)
    author_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    author_name VARCHAR(200) NOT NULL,
    author_email VARCHAR(200),

    -- Comment
    content TEXT NOT NULL,

    -- Moderation
    is_approved BOOLEAN DEFAULT FALSE,
    is_spam BOOLEAN DEFAULT FALSE,

    -- Threading (optional)
    parent_comment_id UUID REFERENCES blog_comments(id) ON DELETE CASCADE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_comments_post ON blog_comments(post_id);
CREATE INDEX idx_blog_comments_author ON blog_comments(author_id);
CREATE INDEX idx_blog_comments_approved ON blog_comments(is_approved);
CREATE INDEX idx_blog_comments_parent ON blog_comments(parent_comment_id);

-- ========== COMMENTS ==========

COMMENT ON TABLE blog_posts IS 'Blog posts with rich content support';
COMMENT ON TABLE blog_post_links IS 'Links to products, services, bundles, and promotions';
COMMENT ON TABLE blog_post_videos IS 'Embedded videos in blog posts';
COMMENT ON TABLE blog_comments IS 'Blog post comments with moderation';

COMMENT ON COLUMN blog_posts.status IS 'DRAFT, PUBLISHED, SCHEDULED, ARCHIVED';
COMMENT ON COLUMN blog_post_links.link_type IS 'PRODUCT, SERVICE, BUNDLE, PROMOTION';
COMMENT ON COLUMN blog_post_videos.video_provider IS 'YOUTUBE, VIMEO, CUSTOM';
COMMENT ON COLUMN blog_post_links.context IS 'Where in the post this link appears (e.g., section name)';
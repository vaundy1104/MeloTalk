// ==========================================
// ユーティリティ関数（最初に定義）
// ==========================================

// XSS対策用のエスケープ関数
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}

// ==========================================
// コメント機能
// ==========================================

// コメント投稿フォーム送信
document.getElementById("commentForm")?.addEventListener("submit", async function(e) {
    e.preventDefault();

    const formData = new FormData(this);
    const data = Object.fromEntries(formData.entries());

    data.songId = document.querySelector("#songId").value;
    data.songTitle = document.querySelector("h3").textContent;
    data.artistId = document.querySelector("#artistId").value;
    data.artistName = document.querySelector(".artist-name").textContent;

    console.log("送信データ:", data);

    // CSRF トークンを取得
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    try {
        const response = await fetch("/api/comments", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [header]: token
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            const newComment = await response.json();

            const commentsList = document.getElementById("comments-list");
            const div = document.createElement("div");
            div.classList.add("comment");
            div.setAttribute('data-comment-id', newComment.commentId);
            div.innerHTML = `
                <div class="comment-author">ユーザー${newComment.user.userName}</div>
                <p class="comment-text">${escapeHtml(newComment.commentText)}</p>
                <div class="comment-actions">
                    <button class="like-btn" onclick="toggleCommentLike(this)">
                        <i class="heart-icon"></i> <span class="like-count">0</span>
                    </button>
                    <button class="reply-btn" onclick="toggleReplyForm(this)">返信する</button>
                    <button class="edit-btn" onclick="editComment(this)">編集</button>
                    <button class="delete-btn" onclick="showDeleteConfirm(this)">削除</button>
                </div>
                <form class="edit-form d-none">
                    <textarea name="commentText" required>${escapeHtml(newComment.commentText)}</textarea>
                    <button type="button" onclick="submitEdit(this)">保存</button>
                    <button type="button" onclick="cancelEdit(this)">キャンセル</button>
                </form>
                <div class="delete-confirm d-none">
                    <p style="color: #ff4444; margin: 10px 0;">本当にこのコメントを削除しますか？</p>
                    <button type="button" onclick="confirmDelete(this)" style="background-color: #ff4444;">削除する</button>
                    <button type="button" onclick="cancelDelete(this)" style="background-color: #666;">キャンセル</button>
                </div>
                <form class="reply-form d-none">
                    <textarea name="commentText" placeholder="返信を書く..." required></textarea>
                    <button type="button" onclick="submitReply(this)">返信を投稿</button>
                </form>
                <div class="replies"></div>
            `;
            commentsList.prepend(div);

            this.reset();
            alert("コメントを投稿しました！");
        } else {
            alert("コメント投稿に失敗しました");
        }
    } catch (error) {
        console.error("コメント投稿エラー:", error);
        alert("コメント投稿中にエラーが発生しました");
    }
});

// いいねボタン
function toggleCommentLike(button) {
    const countSpan = button.querySelector('.like-count');
    let count = parseInt(countSpan.textContent, 10);

    if (button.classList.contains('liked')) {
        button.classList.remove('liked');
        count--;
    } else {
        button.classList.add('liked');
        count++;
    }

    countSpan.textContent = count;
}

// 返信フォーム表示/非表示
function toggleReplyForm(button) {
    const form = button.closest('.comment').querySelector('.reply-form');
    form.classList.toggle('d-none');
}

// 返信投稿
async function submitReply(button) {
    const form = button.closest('form');
    const textarea = form.querySelector('textarea');
    const text = textarea.value.trim();
    
    if (!text) {
        alert("返信内容を入力してください");
        return;
    }

    const parentDiv = button.closest('.comment');
    const parentId = parentDiv.getAttribute('data-comment-id');

    console.log("親コメントID:", parentId);
    console.log("返信テキスト:", text);

    if (!parentId) {
        alert("コメントIDが取得できませんでした");
        return;
    }

    // CSRF トークンを取得
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (!token || !header) {
        alert("CSRFトークンが取得できませんでした");
        return;
    }

    const url = `/api/comments/reply/${parentId}`;

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [header]: token
            },
            body: JSON.stringify({ commentText: text })
        });

        console.log("レスポンスステータス:", response.status);

        if (response.ok) {
            const reply = await response.json();
            
            const replyDiv = document.createElement('div');
            replyDiv.classList.add('comment', 'reply');
            replyDiv.setAttribute('data-comment-id', reply.commentId || reply.id);
            
            // ユーザーIDを取得
            const userId = document.getElementById('userId')?.value;
            
            replyDiv.innerHTML = `
                <div class="comment-author">ユーザー${reply.user.userName}</div>
                <p class="comment-text">${escapeHtml(reply.commentText)}</p>
                <div class="comment-actions">
                    <button class="like-btn" onclick="toggleCommentLike(this)">
                        <i class="heart-icon"></i> <span class="like-count">0</span>
                    </button>
                    <button class="reply-btn" onclick="toggleReplyForm(this)">返信する</button>
                    ${userId && reply.user.userId == userId ? `
                        <button class="edit-btn" onclick="editComment(this)">編集</button>
                        <button class="delete-btn" onclick="showDeleteConfirm(this)">削除</button>
                    ` : ''}
                </div>
                <form class="edit-form d-none">
                    <textarea name="commentText" required>${escapeHtml(reply.commentText)}</textarea>
                    <button type="button" onclick="submitEdit(this)">保存</button>
                    <button type="button" onclick="cancelEdit(this)">キャンセル</button>
                </form>
                <div class="delete-confirm d-none">
                    <p style="color: #ff4444; margin: 10px 0;">本当にこのコメントを削除しますか？</p>
                    <button type="button" onclick="confirmDelete(this)" style="background-color: #ff4444;">削除する</button>
                    <button type="button" onclick="cancelDelete(this)" style="background-color: #666;">キャンセル</button>
                </div>
                <form class="reply-form d-none">
                    <textarea name="commentText" placeholder="返信を書く..." required></textarea>
                    <button type="button" onclick="submitReply(this)">返信を投稿</button>
                </form>
                <div class="replies"></div>
            `;

            parentDiv.querySelector('.replies').appendChild(replyDiv);

            textarea.value = '';
            form.classList.add('d-none');

            alert("返信を投稿しました！");
        } else {
            const contentType = response.headers.get("content-type");
            let errorMessage;
            
            if (contentType && contentType.includes("application/json")) {
                const errorData = await response.json();
                errorMessage = errorData.message || JSON.stringify(errorData);
            } else {
                errorMessage = await response.text();
            }
            
            console.error("返信エラー詳細:", {
                status: response.status,
                statusText: response.statusText,
                message: errorMessage
            });
            
            alert(`返信投稿に失敗しました (${response.status}): ${errorMessage}`);
        }
    } catch (error) {
        console.error("ネットワークエラー:", error);
        alert("返信投稿中にエラーが発生しました: " + error.message);
    }
}

// コメント編集
function editComment(button) {
    const commentDiv = button.closest('.comment');
    const editForm = commentDiv.querySelector('.edit-form');
    const commentText = commentDiv.querySelector('.comment-text');
    const actionsDiv = commentDiv.querySelector('.comment-actions');
    
    // 編集フォームを表示
    editForm.classList.remove('d-none');
    commentText.style.display = 'none';
    actionsDiv.style.display = 'none';
}

// 編集をキャンセル
function cancelEdit(button) {
    const commentDiv = button.closest('.comment');
    const editForm = commentDiv.querySelector('.edit-form');
    const commentText = commentDiv.querySelector('.comment-text');
    const actionsDiv = commentDiv.querySelector('.comment-actions');
    
    editForm.classList.add('d-none');
    commentText.style.display = 'block';
    actionsDiv.style.display = 'flex';
}

// 編集を保存
async function submitEdit(button) {
    const commentDiv = button.closest('.comment');
    const commentId = commentDiv.getAttribute('data-comment-id');
    const textarea = button.closest('.edit-form').querySelector('textarea');
    const newText = textarea.value.trim();
    
    if (!newText) {
        alert("コメント内容を入力してください");
        return;
    }
    
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    
    try {
        const response = await fetch(`/api/comments/${commentId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                [header]: token
            },
            body: JSON.stringify({ commentText: newText })
        });
        
        if (response.ok) {
            const updated = await response.json();
            commentDiv.querySelector('.comment-text').textContent = updated.commentText;
            cancelEdit(button);
            alert("コメントを更新しました！");
        } else {
            alert("コメントの更新に失敗しました");
        }
    } catch (error) {
        console.error("編集エラー:", error);
        alert("編集中にエラーが発生しました");
    }
}

// 削除確認フォームを表示
function showDeleteConfirm(button) {
    const commentDiv = button.closest('.comment');
    const deleteConfirm = commentDiv.querySelector('.delete-confirm');
    const commentText = commentDiv.querySelector('.comment-text');
    const actionsDiv = commentDiv.querySelector('.comment-actions');
    
    // 削除確認フォームを表示
    deleteConfirm.classList.remove('d-none');
    commentText.style.display = 'none';
    actionsDiv.style.display = 'none';
}

// 削除をキャンセル
function cancelDelete(button) {
    const commentDiv = button.closest('.comment');
    const deleteConfirm = commentDiv.querySelector('.delete-confirm');
    const commentText = commentDiv.querySelector('.comment-text');
    const actionsDiv = commentDiv.querySelector('.comment-actions');
    
    deleteConfirm.classList.add('d-none');
    commentText.style.display = 'block';
    actionsDiv.style.display = 'flex';
}

// 削除を実行
async function confirmDelete(button) {
    const commentDiv = button.closest('.comment');
    const commentId = commentDiv.getAttribute('data-comment-id');
    
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    
    try {
        const response = await fetch(`/api/comments/${commentId}`, {
            method: "DELETE",
            headers: {
                [header]: token
            }
        });
        
        if (response.ok) {
            commentDiv.remove();
            alert("コメントを削除しました");
        } else {
            alert("コメントの削除に失敗しました");
            cancelDelete(button);
        }
    } catch (error) {
        console.error("削除エラー:", error);
        alert("削除中にエラーが発生しました");
        cancelDelete(button);
    }
}

// ==========================================
// 検索オートコンプリート機能
// ==========================================

let searchTimeout;
const searchInput = document.getElementById('searchInput');
const searchSuggestions = document.getElementById('searchSuggestions');

if (searchInput && searchSuggestions) {
    console.log('検索オートコンプリート機能を初期化しました');
    
    // 入力時の検索候補表示
    searchInput.addEventListener('input', function(e) {
        clearTimeout(searchTimeout);
        const query = e.target.value.trim();

        if (query.length < 2) {
            hideSuggestions();
            return;
        }

        // 300ms後に検索実行（デバウンス）
        searchTimeout = setTimeout(() => {
            fetchSearchSuggestions(query);
        }, 300);
    });

    // フォーカス時に候補を再表示
    searchInput.addEventListener('focus', function(e) {
        const query = e.target.value.trim();
        if (query.length >= 2) {
            fetchSearchSuggestions(query);
        }
    });

    // 外部クリックで候補を非表示
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !searchSuggestions.contains(e.target)) {
            hideSuggestions();
        }
    });

    // Enterキーで検索実行（候補を非表示にして、その場で表示）
    searchInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            hideSuggestions();
            const query = searchInput.value.trim();
            if (query) {
                // search.htmlの場合はアーティスト検索を実行
                searchArtistByKeyword(query);
            }
        }
    });
}

// 検索候補を取得
async function fetchSearchSuggestions(query) {
    try {
        console.log('検索候補を取得中:', query);
        
        const response = await fetch(`/api/search/suggestions?query=${encodeURIComponent(query)}`);
        
        console.log('レスポンスステータス:', response.status);
        
        if (response.ok) {
            const data = await response.json();
            console.log('取得したデータ:', data);
            displaySuggestions(data, query);
        } else {
            console.error('検索候補の取得に失敗:', response.status);
        }
    } catch (error) {
        console.error('検索候補の取得エラー:', error);
    }
}

// 検索候補を表示
function displaySuggestions(data, query) {
    if (!searchSuggestions) return;
    
    const hasResults = data.songs && data.songs.length > 0 || data.artists && data.artists.length > 0;

    if (!hasResults) {
        searchSuggestions.innerHTML = `
            <div class="no-results">
                「${escapeHtml(query)}」の検索結果が見つかりませんでした
            </div>
        `;
        searchSuggestions.classList.add('show');
        return;
    }

    let html = '';

    // 楽曲セクション
    if (data.songs && data.songs.length > 0) {
        html += `
            <div class="suggestions-section">
                <div class="suggestions-title">楽曲</div>
                ${data.songs.map(song => `
                    <a href="/song?songId=${encodeURIComponent(song.songId)}&title=${encodeURIComponent(song.songTitle)}&artist=${encodeURIComponent(song.artistName)}" 
                       class="suggestion-item">
                        <div class="suggestion-icon">
                            <svg viewBox="0 0 24 24">
                                <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
                            </svg>
                        </div>
                        <div class="suggestion-details">
                            <div class="suggestion-name">${highlightMatch(song.songTitle, query)}</div>
                            <div class="suggestion-artist">${escapeHtml(song.artistName)}</div>
                        </div>
                    </a>
                `).join('')}
            </div>
        `;
    }

    // アーティストセクション（アーティスト名ではなくアーティストIDで検索）
    if (data.artists && data.artists.length > 0) {
        html += `
            <div class="suggestions-section">
                <div class="suggestions-title">アーティスト</div>
                ${data.artists.map(artist => `
                    <div class="suggestion-item" onclick="searchArtistById('${escapeHtml(artist.artistId)}', '${escapeHtml(artist.artistName).replace(/'/g, "\\'")}')">
                        <div class="suggestion-icon">
                            <svg viewBox="0 0 24 24">
                                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                            </svg>
                        </div>
                        <div class="suggestion-details">
                            <div class="suggestion-name">${highlightMatch(artist.artistName, query)}</div>
                            <div class="suggestion-artist">アーティスト</div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    searchSuggestions.innerHTML = html;
    searchSuggestions.classList.add('show');
}

// 検索候補を非表示
function hideSuggestions() {
    if (searchSuggestions) {
        searchSuggestions.classList.remove('show');
    }
}

// 検索クエリをハイライト
function highlightMatch(text, query) {
    if (!text || !query) return escapeHtml(text || '');
    const escapedText = escapeHtml(text);
    const escapedQuery = escapeHtml(query).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`(${escapedQuery})`, 'gi');
    return escapedText.replace(regex, '<strong>$1</strong>');
}

// ==========================================
// search.html専用：アーティスト検索機能
// ==========================================

// アーティストIDで検索（候補クリック時に使用）
async function searchArtistById(artistId, artistName) {
    if (!artistId) {
        alert('アーティストIDが取得できませんでした');
        return;
    }

    hideSuggestions();
    
    // 検索ボックスの値を更新
    if (searchInput) {
        searchInput.value = artistName;
    }

    try {
        console.log('アーティストIDで検索中:', artistId);
        const response = await fetch(`/api/spotify/artist-detail-by-id?artistId=${encodeURIComponent(artistId)}`);
        const data = await response.json();

        sessionStorage.setItem('lastSearchKeyword', artistName);
        sessionStorage.setItem('lastSearchResult', JSON.stringify(data));

        displaySearchResults(data);
    } catch (error) {
        console.error('検索エラー:', error);
        const resultsDiv = document.getElementById('results');
        if (resultsDiv) {
            resultsDiv.innerHTML = '<p style="color: red;">検索中にエラーが発生しました。</p>';
        }
    }
}

// キーワードで検索（Enterキー押下時に使用）
async function searchArtistByKeyword(keyword) {
    if (!keyword) {
        alert('検索ワードを入力してください');
        return;
    }

    hideSuggestions();
    
    // 検索ボックスの値を更新
    if (searchInput) {
        searchInput.value = keyword;
    }

    try {
        console.log('キーワードで検索中:', keyword);
        const response = await fetch(`/api/spotify/artist-detail?keyword=${encodeURIComponent(keyword)}`);
        const data = await response.json();

        sessionStorage.setItem('lastSearchKeyword', keyword);
        sessionStorage.setItem('lastSearchResult', JSON.stringify(data));

        displaySearchResults(data);
    } catch (error) {
        console.error('検索エラー:', error);
        const resultsDiv = document.getElementById('results');
        if (resultsDiv) {
            resultsDiv.innerHTML = '<p style="color: red;">検索中にエラーが発生しました。アーティスト名を変更して再度お試しください。</p>';
        }
    }
}

function displaySearchResults(data) {
    const resultsDiv = document.getElementById('results');
    if (!resultsDiv) return;
    
    resultsDiv.innerHTML = '';

    if (!data.artist) {
        resultsDiv.innerHTML = '<p>検索結果がありません。</p>';
        return;
    }

    // アーティスト情報
    const artistInfo = document.createElement('div');
    artistInfo.classList.add('artist-info');
    artistInfo.innerHTML = `
        <img src="${data.artist.image}" alt="${data.artist.name}">
        <div class="meta">
            <h2>${data.artist.name}</h2>
            <p><strong>ジャンル:</strong> ${data.artist.genres.join(', ')}</p>
            <p><strong>フォロワー:</strong> ${data.artist.followers.toLocaleString()}</p>
        </div>
    `;
    resultsDiv.appendChild(artistInfo);

    // 人気曲
    const tracksTitle = document.createElement('h3');
    tracksTitle.textContent = '人気曲';
    resultsDiv.appendChild(tracksTitle);

    const trackList = document.createElement('div');
    trackList.classList.add('track-list');
    data.topTracks.forEach(track => {
        const trackItem = document.createElement('div');
        trackItem.classList.add('track-item');
        const songId = track.songId;
        if (!songId) return;

        trackItem.innerHTML = `
            <div class="track-thumb">
                <img src="${encodeURI(track.image || '')}" alt="${track.album}">
            </div>
            <div class="track-info">
                <p title="${track.name}"><a href="/song?songId=${encodeURIComponent(songId)}&title=${encodeURIComponent(track.name)}&artist=${encodeURIComponent(data.artist.name)}&artistId=${encodeURIComponent(data.artist.id)}"><strong>${track.name}</strong></a></p>
                <p title="${track.album}"><a href="/album.html?albumId=${encodeURIComponent(track.albumId)}&artist=${encodeURIComponent(data.artist.name)}">${track.album}</a> ・ ${track.releaseDate}</p>
            </div>
        `;
        trackList.appendChild(trackItem);
    });

    resultsDiv.appendChild(trackList);

    // アルバム
    const albumsTitle = document.createElement('h3');
    albumsTitle.textContent = 'アルバム';
    resultsDiv.appendChild(albumsTitle);

    const albumsDiv = document.createElement('div');
    albumsDiv.classList.add('albums');
    resultsDiv.appendChild(albumsDiv);

    let showingAll = false;

    function renderAlbums() {
        albumsDiv.innerHTML = '';
        const albumsToShow = showingAll ? data.albums : data.albums.slice(0, 6);

        albumsToShow.forEach(album => {
            const albumCard = document.createElement('div');
            albumCard.classList.add('album-card');
            albumCard.innerHTML = `
                <a href="/album.html?albumId=${encodeURIComponent(album.id)}&artist=${encodeURIComponent(data.artist.name)}">
                    <img src="${album.image}" alt="${album.name}">
                    <p title="${album.name}"><strong>${album.name}</strong></p>
                    <p>${album.releaseDate}</p>
                </a>
                <div class="play-button">▶</div>
            `;
            albumsDiv.appendChild(albumCard);
        });

        if (data.albums.length > 6) {
            if (!document.getElementById('toggleAlbumsBtn')) {
                const toggleBtn = document.createElement('button');
                toggleBtn.id = 'toggleAlbumsBtn';
                toggleBtn.style.marginTop = '20px';
                toggleBtn.style.backgroundColor = '#1DB954';
                toggleBtn.style.color = 'white';
                toggleBtn.style.border = 'none';
                toggleBtn.style.borderRadius = '20px';
                toggleBtn.style.padding = '10px 20px';
                toggleBtn.style.cursor = 'pointer';
                resultsDiv.appendChild(toggleBtn);
            }
            const toggleBtn = document.getElementById('toggleAlbumsBtn');
            toggleBtn.textContent = showingAll ? '閉じる' : 'もっと見る';
            toggleBtn.onclick = () => {
                showingAll = !showingAll;
                renderAlbums();
            };
        }
    }

    renderAlbums();
}

// ==========================================
// ロゴクリック時の検索履歴クリア
// ==========================================

// ロゴをクリックしたときに検索履歴をクリアする関数
function clearSearchHistory() {
    sessionStorage.removeItem('lastSearchResult');
    sessionStorage.removeItem('lastSearchKeyword');
    console.log('検索履歴をクリアしました');
}

// ==========================================
// ページ初期化
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    // ロゴのクリックイベントを設定
    const logoLink = document.querySelector('.logo-link');
    if (logoLink && logoLink.parentElement) {
        logoLink.parentElement.addEventListener('click', function(e) {
            // 検索履歴をクリア
            clearSearchHistory();
        });
    }

    // songページ用の初期化
    const params = new URLSearchParams(window.location.search);
    const trackName = params.get('trackName');
    const artistName = params.get('artistName');

    if (trackName && artistName) {
        const h3 = document.querySelector('h3');
        const artistEl = document.querySelector('.artist-name');
        
        if (h3) h3.textContent = trackName;
        if (artistEl) artistEl.textContent = artistName;
    }
    
    // search.html用の初期化（前回の検索結果を復元）
    const lastResult = sessionStorage.getItem('lastSearchResult');
    const lastKeyword = sessionStorage.getItem('lastSearchKeyword');
    if (lastResult && lastKeyword && searchInput) {
        searchInput.value = lastKeyword;
        displaySearchResults(JSON.parse(lastResult));
    }
    
    console.log('music-share.js が読み込まれました');
});
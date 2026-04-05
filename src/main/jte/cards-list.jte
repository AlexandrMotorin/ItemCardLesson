<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>English Words - Learn Vocabulary</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }
        header h1 {
            font-size: 2.5rem;
            margin-bottom: 10px;
        }
        .nav-links {
            display: flex;
            justify-content: center;
            gap: 15px;
            margin-top: 20px;
        }
        .nav-links a {
            color: white;
            text-decoration: none;
            padding: 10px 20px;
            background: rgba(255,255,255,0.2);
            border-radius: 5px;
            transition: background 0.3s;
        }
        .nav-links a:hover {
            background: rgba(255,255,255,0.3);
        }
        .content {
            background: white;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }
        .success-message {
            background: #d4edda;
            color: #155724;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .search-form {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
        }
        .search-form input {
            flex: 1;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 1rem;
        }
        .btn {
            padding: 10px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            transition: background 0.3s;
        }
        .btn:hover {
            background: #5568d3;
        }
        .btn-danger {
            background: #dc3545;
        }
        .btn-danger:hover {
            background: #c82333;
        }
        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        .card-item {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            border-left: 4px solid #667eea;
        }
        .card-item h3 {
            color: #333;
            margin-bottom: 10px;
        }
        .card-item p {
            color: #666;
            margin-bottom: 5px;
        }
        .card-actions {
            margin-top: 15px;
            display: flex;
            gap: 10px;
        }
        .difficulty-badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 3px;
            font-size: 0.8rem;
            margin-top: 10px;
        }
        .difficulty-BEGINNER { background: #d4edda; color: #155724; }
        .difficulty-INTERMEDIATE { background: #fff3cd; color: #856404; }
        .difficulty-ADVANCED { background: #f8d7da; color: #721c24; }
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>📚 English Words App</h1>
            <p>Master English vocabulary with flashcards</p>
            <div class="nav-links">
                <a href="/cards">All Cards</a>
                <a href="/cards/new">Add New Card</a>
                <a href="/cards/random">Random Card</a>
            </div>
        </header>

        <div class="content">
            <if test="${successMessage != null}">
                <div class="success-message">${successMessage}</div>
            </if>

            <h2>Word Cards</h2>

            <form class="search-form" action="/cards/search" method="GET">
                <input type="text" name="query" placeholder="Search for words..." value="${query ?? ''}">
                <button type="submit" class="btn">Search</button>
            </form>

            <if test="${cards.isEmpty()}">
                <div class="empty-state">
                    <p>No word cards found.</p>
                    <p><a href="/cards/new" class="btn">Create your first card!</a></p>
                </div>
            </if>

            <div class="cards-grid">
                <for items="${cards}" var="card">
                    <div class="card-item">
                        <h3>${card.englishWord}</h3>
                        <p><strong>Translation:</strong> ${card.translation}</p>
                        <if test="${card.example != null && !card.example.isEmpty()}">
                            <p><em>Example:</em> ${card.example}</p>
                        </if>
                        <span class="difficulty-badge difficulty-${card.difficultyLevel}">${card.difficultyLevel}</span>
                        <div class="card-actions">
                            <a href="/cards/${card.id}" class="btn">View</a>
                            <a href="/cards/${card.id}/edit" class="btn">Edit</a>
                            <form action="/cards/${card.id}/delete" method="POST" style="display:inline;">
                                <button type="submit" class="btn btn-danger" onclick="return confirm('Are you sure?')">Delete</button>
                            </form>
                        </div>
                    </div>
                </for>
            </div>
        </div>
    </div>
</body>
</html>

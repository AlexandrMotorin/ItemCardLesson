<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${card.englishWord} - Word Card</title>
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
            max-width: 700px;
            margin: 0 auto;
        }
        header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }
        header h1 {
            font-size: 2rem;
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
        }
        .content {
            background: white;
            border-radius: 10px;
            padding: 40px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }
        .flashcard {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            border-radius: 15px;
            padding: 40px;
            text-align: center;
            margin-bottom: 30px;
            border-left: 6px solid #667eea;
        }
        .flashcard h2 {
            font-size: 2.5rem;
            color: #333;
            margin-bottom: 20px;
        }
        .translation {
            font-size: 1.5rem;
            color: #667eea;
            margin-bottom: 20px;
            padding-top: 20px;
            border-top: 2px solid #ddd;
        }
        .example {
            font-style: italic;
            color: #666;
            margin: 20px 0;
            padding: 15px;
            background: white;
            border-radius: 8px;
        }
        .notes {
            color: #555;
            margin: 20px 0;
            padding: 15px;
            background: #fff3cd;
            border-radius: 8px;
        }
        .difficulty-badge {
            display: inline-block;
            padding: 8px 16px;
            border-radius: 5px;
            font-size: 1rem;
            font-weight: 600;
            margin-top: 15px;
        }
        .difficulty-BEGINNER { background: #d4edda; color: #155724; }
        .difficulty-INTERMEDIATE { background: #fff3cd; color: #856404; }
        .difficulty-ADVANCED { background: #f8d7da; color: #721c24; }
        .actions {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-top: 30px;
        }
        .btn {
            padding: 12px 24px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            font-size: 1rem;
            transition: background 0.3s;
        }
        .btn:hover {
            background: #5568d3;
        }
        .btn-secondary {
            background: #6c757d;
        }
        .btn-secondary:hover {
            background: #5a6268;
        }
        .btn-danger {
            background: #dc3545;
        }
        .btn-danger:hover {
            background: #c82333;
        }
        .btn-success {
            background: #28a745;
        }
        .btn-success:hover {
            background: #218838;
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>📖 Word Card Details</h1>
            <div class="nav-links">
                <a href="/cards">All Cards</a>
                <a href="/cards/new">Add New</a>
                <a href="/cards/random">Random Card</a>
            </div>
        </header>

        <div class="content">
            <div class="flashcard">
                <h2>${card.englishWord}</h2>
                <div class="translation">${card.translation}</div>
                
                <if test="${card.example != null && !card.example.isEmpty()}">
                    <div class="example">
                        <strong>Example:</strong><br>
                        ${card.example}
                    </div>
                </if>
                
                <if test="${card.notes != null && !card.notes.isEmpty()}">
                    <div class="notes">
                        <strong>Notes:</strong><br>
                        ${card.notes}
                    </div>
                </if>
                
                <span class="difficulty-badge difficulty-${card.difficultyLevel}">
                    ${card.difficultyLevel}
                </span>
            </div>

            <div class="actions">
                <a href="/cards/${card.id}/edit" class="btn">Edit</a>
                <a href="/cards/random" class="btn btn-success">Next Random</a>
                <form action="/cards/${card.id}/delete" method="POST" style="display:inline;">
                    <button type="submit" class="btn btn-danger" onclick="return confirm('Are you sure you want to delete this card?')">Delete</button>
                </form>
                <a href="/cards" class="btn btn-secondary">Back to List</a>
            </div>
        </div>
    </div>
</body>
</html>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${wordCard.id != null ? 'Edit' : 'Add'} Word Card</title>
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
            max-width: 600px;
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
            padding: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: 600;
            color: #333;
        }
        .form-group input,
        .form-group textarea,
        .form-group select {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 1rem;
        }
        .form-group textarea {
            resize: vertical;
            min-height: 100px;
        }
        .error-message {
            color: #dc3545;
            font-size: 0.875rem;
            margin-top: 5px;
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
        .form-actions {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>${wordCard.id != null ? '✏️ Edit' : '➕ Add'} Word Card</h1>
            <div class="nav-links">
                <a href="/cards">Back to Cards</a>
            </div>
        </header>

        <div class="content">
            <form method="POST" 
                  action="${wordCard.id != null ? '/cards/' + wordCard.id : '/cards'}">
                
                <div class="form-group">
                    <label for="englishWord">English Word *</label>
                    <input type="text" id="englishWord" name="englishWord" 
                           value="${wordCard.englishWord ?? ''}" required>
                    <if test="${errors.hasFieldErrors('englishWord')}">
                        <div class="error-message">${errors.getFieldError('englishWord').getDefaultMessage()}</div>
                    </if>
                </div>

                <div class="form-group">
                    <label for="translation">Translation (Your Language) *</label>
                    <input type="text" id="translation" name="translation" 
                           value="${wordCard.translation ?? ''}" required>
                    <if test="${errors.hasFieldErrors('translation')}">
                        <div class="error-message">${errors.getFieldError('translation').getDefaultMessage()}</div>
                    </if>
                </div>

                <div class="form-group">
                    <label for="example">Example Sentence</label>
                    <textarea id="example" name="example" 
                              placeholder="Enter an example sentence using this word...">${wordCard.example ?? ''}</textarea>
                </div>

                <div class="form-group">
                    <label for="notes">Notes</label>
                    <textarea id="notes" name="notes" 
                              placeholder="Additional notes about this word...">${wordCard.notes ?? ''}</textarea>
                </div>

                <div class="form-group">
                    <label for="difficultyLevel">Difficulty Level</label>
                    <select id="difficultyLevel" name="difficultyLevel">
                        <option value="BEGINNER" ${wordCard.difficultyLevel == null || wordCard.difficultyLevel.name() == 'BEGINNER' ? 'selected' : ''}>Beginner</option>
                        <option value="INTERMEDIATE" ${wordCard.difficultyLevel != null && wordCard.difficultyLevel.name() == 'INTERMEDIATE' ? 'selected' : ''}>Intermediate</option>
                        <option value="ADVANCED" ${wordCard.difficultyLevel != null && wordCard.difficultyLevel.name() == 'ADVANCED' ? 'selected' : ''}>Advanced</option>
                    </select>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn">${wordCard.id != null ? 'Update' : 'Create'} Card</button>
                    <a href="/cards" class="btn btn-secondary">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>

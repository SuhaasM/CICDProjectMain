import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import api from '../utils/api';
import { toast } from 'react-toastify';
import { Container, Title, Text, Paper, Button, Accordion, Textarea, Select, Group, Loader, List, TextInput, Modal, Radio, Stack, Switch } from '@mantine/core';

// Helper function to convert YouTube URL to embeddable format
const getYouTubeEmbedUrl = (url) => {
    if (!url) return '';
    let videoId = '';
    if (url.includes('watch?v=')) {
        videoId = url.split('watch?v=')[1].split('&')[0];
    } else if (url.includes('youtu.be/')) {
        videoId = url.split('youtu.be/')[1].split('?')[0];
    }
    return `https://www.youtube.com/embed/${videoId}`;
};

function CourseDetailPage() {
    const { courseId } = useParams();
    const { user, token } = useAuth();
    const [lessons, setLessons] = useState([]);
    const [course, setCourse] = useState(null);
    const [loading, setLoading] = useState(true);
    const [quiz, setQuiz] = useState(null);
    const [editingQuiz, setEditingQuiz] = useState(null);
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [quizVisibility, setQuizVisibility] = useState(false);
    const [studentAnswers, setStudentAnswers] = useState({});
    const [submittedQuestions, setSubmittedQuestions] = useState([]);
    
    const [showAddLessonForm, setShowAddLessonForm] = useState(false);
    const [lessonTitle, setLessonTitle] = useState('');
    const [lessonType, setLessonType] = useState('text');
    const [lessonContent, setLessonContent] = useState('');

    const fetchCourseAndLessons = async () => {
        if (!token) return;
        try {
            const [courseRes, lessonsRes] = await Promise.all([
                api.get(`/api/courses/${courseId}`),
                api.get(`/api/courses/${courseId}/lessons`)
            ]);
            setCourse(courseRes.data);
            setLessons(lessonsRes.data);
            
            // If there are lessons, check for quizzes
            if (lessonsRes.data && lessonsRes.data.length > 0) {
                const currentLesson = lessonsRes.data[0];
                fetchQuizForLesson(currentLesson.id);
            }
        } catch (error) {
            toast.error(error?.response?.data?.error || "Failed to fetch course data.");
        } finally {
            setLoading(false);
        }
    };
    
    const fetchQuizForLesson = async (lessonId) => {
        try {
            const response = await api.get(`/api/ai/quiz/${lessonId}`);
            if (response.data) {
                setQuiz(response.data);
                // Reset student answers when loading a new quiz
                setStudentAnswers({});
                setSubmittedQuestions([]);
            }
        } catch (error) {
            console.error("Failed to fetch quiz:", error);
        }
    };

    useEffect(() => {
        fetchCourseAndLessons();
    }, [courseId, token]);

    const handleAddLesson = async (e) => {
        e.preventDefault();
        const payload = {
            title: lessonTitle, contentType: lessonType,
            contentUrl: lessonType === 'video' ? lessonContent : null,
            contentText: lessonType === 'text' ? lessonContent : null,
        };
        try {
            await api.post(`/api/courses/${courseId}/lessons`, payload);
            toast.success("Lesson added successfully!");
            setLessonTitle(''); setLessonType('text'); setLessonContent('');
            setShowAddLessonForm(false);
            fetchCourseAndLessons();
        } catch (error) {
            toast.error(error?.response?.data?.error || "Failed to add lesson.");
        }
    };

    const handleGenerateQuiz = async (lesson) => {
        if (!lesson.contentText) {
            toast.warn("This lesson has no text content to generate a quiz from.");
            return;
        }
        try {
            const response = await api.post('/api/ai/generate-quiz', { text: lesson.contentText, lessonId: lesson.id });
            setQuiz(response.data);
            setEditingQuiz(response.data);
            setEditModalOpen(true);
            toast.success("Quiz generated successfully! You can now edit the questions.");
        } catch (error) {
            toast.error(error?.response?.data?.error || "Failed to generate quiz.");
        }
    };
    
    const handleSaveQuiz = async () => {
        try {
            await api.post('/api/ai/save-quiz', { 
                quiz: editingQuiz, 
                lessonId: lessons.find(l => l.id === editingQuiz.lessonId)?.id,
                visible: true // Always make quiz visible to students
            });
            setQuiz(editingQuiz);
            setEditModalOpen(false);
            toast.success("Quiz saved successfully! Students can now access this quiz.");
        } catch (error) {
            toast.error(error?.response?.data?.error || "Failed to save quiz.");
            console.error(error);
        }
    };
    
    const handleEditQuestion = (index, field, value) => {
        const updatedQuiz = {...editingQuiz};
        updatedQuiz.questions[index][field] = value;
        setEditingQuiz(updatedQuiz);
    };
    
    const handleEditOption = (questionIndex, optionIndex, value) => {
        const updatedQuiz = {...editingQuiz};
        updatedQuiz.questions[questionIndex].options[optionIndex] = value;
        setEditingQuiz(updatedQuiz);
    };
    
    const handleAnswerSelect = (questionIndex, answerIndex) => {
        setStudentAnswers({
            ...studentAnswers,
            [questionIndex]: answerIndex
        });
    };
    
    const handleSubmitAnswer = (questionIndex) => {
        if (submittedQuestions.includes(questionIndex)) return;
        setSubmittedQuestions([...submittedQuestions, questionIndex]);
        
        // Calculate and show score after submission
        const correctAnswers = submittedQuestions.filter(qIndex => 
            studentAnswers[qIndex] === quiz.questions[qIndex].correctAnswerIndex
        ).length;
        
        // If all questions are answered, show a toast with the final score
        if (submittedQuestions.length + 1 === quiz.questions.length) {
            const finalScore = correctAnswers + (studentAnswers[questionIndex] === quiz.questions[questionIndex].correctAnswerIndex ? 1 : 0);
            toast.success(`Quiz completed! Your final score: ${finalScore}/${quiz.questions.length}`);
        }
    };
    
    const handleSubmitAllAnswers = () => {
        // Submit all remaining questions
        const allQuestions = Array.from({length: quiz.questions.length}, (_, i) => i);
        setSubmittedQuestions(allQuestions);
        
        // Calculate final score
        const correctAnswers = allQuestions.filter(qIndex => 
            studentAnswers[qIndex] === quiz.questions[qIndex].correctAnswerIndex
        ).length;
        
        toast.success(`Quiz completed! Your final score: ${correctAnswers}/${quiz.questions.length}`);
    };

    if (loading) return <Container my="lg"><Loader /></Container>;
    if (!course) return <Container my="lg"><Text>Course not found.</Text></Container>;

    const isInstructor = user?.id === course?.instructor?.id;

    return (
        <Container my="lg">
            <Title order={2}>{course.title}</Title>
            <Text c="dimmed" mb="lg">{course.description}</Text>
            
            <Title order={3} mb="md">Lessons</Title>
            <Accordion variant="separated">
                {lessons.map(lesson => (
                    <Accordion.Item 
                        value={lesson.title} 
                        key={lesson.id}
                        onChange={() => fetchQuizForLesson(lesson.id)}
                    >
                        <Accordion.Control>{lesson.title}</Accordion.Control>
                        <Accordion.Panel>
                            {lesson.contentType === 'video' && lesson.contentUrl && (
                                <div className="video-responsive">
                                    <iframe width="560" height="315" src={getYouTubeEmbedUrl(lesson.contentUrl)} title={lesson.title} frameBorder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowFullScreen></iframe>
                                </div>
                            )}
                            {lesson.contentType === 'text' && <Text>{lesson.contentText}</Text>}
                            {isInstructor && lesson.contentType === 'text' && (
                                <Button 
                                    onClick={() => handleGenerateQuiz(lesson)} 
                                    size="xs" 
                                    mt="md"
                                    color="blue"
                                    leftIcon={<span>✨</span>}
                                >
                                    Generate Quiz with AI
                                </Button>
                            )}
                        </Accordion.Panel>
                    </Accordion.Item>
                ))}
            </Accordion>

            {quiz && (
                <Paper withBorder shadow="md" p="md" mt="xl">
                    <Title order={4}>{quiz.title}</Title>
                    {quiz.questions && Array.isArray(quiz.questions) ? (
                        <>
                            {quiz.questions.map((q, index) => (
                                <div key={index} style={{ marginTop: '1rem' }}>
                                    <Text fw={500}>{index + 1}. {q.questionText}</Text>
                                    
                                    {isInstructor ? (
                                        // Faculty view - always shows correct answers
                                        <List listStyleType="none" ml="md">
                                            {q.options.map((opt, i) => (
                                                <List.Item key={i} c={i === q.correctAnswerIndex ? 'green' : 'dimmed'}>
                                                    {opt} {i === q.correctAnswerIndex && '(Correct Answer)'}
                                                </List.Item>
                                            ))}
                                        </List>
                                    ) : (
                                        // Student view - only shows options first, then correct answer after submission
                                        <>
                                            <Radio.Group 
                                                value={studentAnswers[index]?.toString()} 
                                                onChange={(value) => handleAnswerSelect(index, parseInt(value))}
                                                name={`question-${index}`}
                                                disabled={submittedQuestions.includes(index)}
                                            >
                                                <Stack mt="xs">
                                                    {q.options.map((opt, i) => (
                                                        <Radio key={i} value={i.toString()} label={opt} />
                                                    ))}
                                                </Stack>
                                            </Radio.Group>
                                            
                                            {!submittedQuestions.includes(index) ? (
                                                <Button 
                                                    onClick={() => handleSubmitAnswer(index)} 
                                                    disabled={studentAnswers[index] === undefined}
                                                    size="xs" 
                                                    mt="sm"
                                                >
                                                    Submit Answer
                                                </Button>
                                            ) : (
                                                <Text mt="sm" c={studentAnswers[index] === q.correctAnswerIndex ? 'green' : 'red'}>
                                                    {studentAnswers[index] === q.correctAnswerIndex 
                                                        ? 'Correct!' 
                                                        : `Incorrect. The correct answer is: ${q.options[q.correctAnswerIndex]}`}
                                                </Text>
                                            )}
                                        </>
                                    )}
                                </div>
                            ))}
                            
                            {!isInstructor && (
                                <Paper withBorder p="md" mt="xl" bg="blue.0">
                                    <Group position="apart" align="center">
                                        <div>
                                            <Text fw={700}>Your Score: {submittedQuestions.filter(qIndex => 
                                                studentAnswers[qIndex] === quiz.questions[qIndex].correctAnswerIndex
                                            ).length} / {submittedQuestions.length}</Text>
                                            <Text size="sm" c="dimmed">
                                                {submittedQuestions.length === quiz.questions.length 
                                                    ? 'Quiz completed!' 
                                                    : `${quiz.questions.length - submittedQuestions.length} questions remaining`}
                                            </Text>
                                        </div>
                                        {submittedQuestions.length < quiz.questions.length && (
                                            <Button 
                                                onClick={handleSubmitAllAnswers}
                                                color="blue"
                                            >
                                                Submit All Answers
                                            </Button>
                                        )}
                                    </Group>
                                </Paper>
                            )}
                        </>
                    ) : <Text c="dimmed">Quiz questions not available.</Text>}
                </Paper>
            )}
            
            {/* Quiz Editing Modal */}
            <Modal 
                opened={editModalOpen} 
                onClose={() => setEditModalOpen(false)}
                title="Edit Quiz Questions"
                size="lg"
            >
                {editingQuiz && (
                    <>
                        <TextInput 
                            label="Quiz Title" 
                            value={editingQuiz.title || ''} 
                            onChange={(e) => setEditingQuiz({...editingQuiz, title: e.target.value})}
                            mb="md"
                        />
                        
                        <Switch 
                            label="Make quiz visible to students" 
                            checked={quizVisibility}
                            onChange={(e) => setQuizVisibility(e.currentTarget.checked)}
                            mb="xl"
                        />
                        
                        {editingQuiz.questions && editingQuiz.questions.map((question, qIndex) => (
                            <Paper key={qIndex} withBorder p="md" mb="md">
                                <TextInput 
                                    label={`Question ${qIndex + 1}`}
                                    value={question.questionText}
                                    onChange={(e) => handleEditQuestion(qIndex, 'questionText', e.target.value)}
                                    mb="sm"
                                />
                                
                                {question.options.map((option, oIndex) => (
                                    <Group key={oIndex} mb="xs">
                                        <TextInput 
                                            label={`Option ${oIndex + 1}`}
                                            value={option}
                                            onChange={(e) => handleEditOption(qIndex, oIndex, e.target.value)}
                                            style={{ flex: 1 }}
                                        />
                                        <Radio 
                                            checked={question.correctAnswerIndex === oIndex}
                                            onChange={() => handleEditQuestion(qIndex, 'correctAnswerIndex', oIndex)}
                                            label="Correct"
                                            mt="lg"
                                        />
                                    </Group>
                                ))}
                            </Paper>
                        ))}
                        
                        <Group position="right" mt="xl">
                            <Button variant="outline" onClick={() => setEditModalOpen(false)}>Cancel</Button>
                            <Button onClick={handleSaveQuiz}>Save Quiz</Button>
                        </Group>
                    </>
                )}
            </Modal>

            {isInstructor && (
                <Paper withborder shadow="md" p="md" mt="xl">
                    <Button onClick={() => setShowAddLessonForm(!showAddLessonForm)} variant="light">
                        {showAddLessonForm ? 'Cancel' : 'Add New Lesson'}
                    </Button>
                    {showAddLessonForm && (
                        <form onSubmit={handleAddLesson} style={{ marginTop: '1rem' }}>
                            <TextInput value={lessonTitle} onChange={(e) => setLessonTitle(e.target.value)} label="Lesson Title" required />
                            <Select value={lessonType} onChange={setLessonType} label="Lesson Type" data={['text', 'video']} mt="md" />
                            <Textarea value={lessonContent} onChange={(e) => setLessonContent(e.target.value)} label="Content" placeholder={lessonType === 'video' ? 'Enter YouTube URL' : 'Enter lesson text'} required mt="md" />
                            <Button type="submit" mt="md">Save Lesson</Button>
                        </form>
                    )}
                </Paper>
            )}
        </Container>
    );
}

export default CourseDetailPage;
